/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.ignite;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.annotation.sql.SqlMembers;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.runtime.BatchOperation;
import io.micronaut.data.model.runtime.EntityOperation;
import io.micronaut.data.model.runtime.InsertOperation;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import io.micronaut.data.model.runtime.UpdateOperation;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.runtime.config.DataSettings;
import io.micronaut.data.runtime.date.DateTimeProvider;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.MediaTypeCodec;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.micronaut.data.runtime.config.DataSettings.QUERY_LOG;

/**
 * Implementation of the {@link RepositoryOperations} interface for Ignite.
 */
@EachBean(Ignite.class)
public class IgniteRepositoryOperations implements RepositoryOperations {

    protected static final IgniteSqlQueryBuilder DEFAULT_IGNITE_QUERY_BUILDER = new IgniteSqlQueryBuilder();

    private final Ignite ignite;
    private final BeanContext beanContext;
    private final Map<Class, RuntimePersistentEntity> entities = new ConcurrentHashMap<>(10);
    protected final MediaTypeCodec jsonCodec;
    protected final DateTimeProvider dateTimeProvider;


    public IgniteRepositoryOperations(Ignite ignite,
                                      List<MediaTypeCodec> codecs,
                                      BeanContext beanContext,
    @NonNull DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
        this.ignite = ignite;
        this.beanContext = beanContext;
        this.jsonCodec = resolveJsonCodec(codecs);
    }


    private MediaTypeCodec resolveJsonCodec(List<MediaTypeCodec> codecs) {
        return CollectionUtils.isNotEmpty(codecs) ? codecs.stream().filter(c -> c.getMediaTypes().contains(MediaType.APPLICATION_JSON_TYPE)).findFirst().orElse(null) : null;
    }


    public <K, V> IgniteCache<K, V> resolveCache(AnnotationMetadata metadata) {
        Optional<String> schema = metadata.stringValue(MappedEntity.class, SqlMembers.SCHEMA);
        if (!schema.isPresent())
            throw new IllegalStateException("Mapped Schema not found");
        return ignite.cache(schema.get());
    }

    @Nullable
    @Override
    public <T> T findOne(@NonNull Class<T> type, @NonNull Serializable id) {
        throw new UnsupportedOperationException("The findOne method by ID is not supported. Execute the SQL query directly");
    }

    @NonNull
    @Override
    public <T> RuntimePersistentEntity<T> getEntity(@NonNull Class<T> type) {
        ArgumentUtils.requireNonNull("type", type);
        RuntimePersistentEntity<T> entity = entities.get(type);
        if (entity == null) {
            entity = new RuntimePersistentEntity<T>(type) {
                @Override
                protected RuntimePersistentEntity<T> getEntity(Class<T> type) {
                    return IgniteRepositoryOperations.this.getEntity(type);
                }
            };
            entities.put(type, entity);
        }
        return entity;
    }

    public <T, R> SqlFieldsQuery prepareStatement(String query, int[] bindings, Object[] targets) {
        SqlFieldsQuery fieldsQuery = new SqlFieldsQuery(query);
        Object[] args = new Object[bindings.length];
        for (int i = 0; i < bindings.length; i++) {
            int parameterIndex = bindings[i];
            Object value = targets[parameterIndex];
            args[i] = value;
        }
        fieldsQuery.setArgs(args);
        return fieldsQuery;
    }


    @Nullable
    @Override
    public <T, R> R findOne(@NonNull PreparedQuery<T, R> preparedQuery) {
        final Class<R> resultType = preparedQuery.getResultType();
        final AnnotationMetadata annotationMetadata = preparedQuery.getAnnotationMetadata();
        final IgniteCache<T, R> cache = resolveCache(annotationMetadata);
        SqlFieldsQuery fieldsQuery = prepareStatement(
            preparedQuery.getQuery(),
            preparedQuery.getIndexedParameterBinding(),
            preparedQuery.getParameterArray());

        try (FieldsQueryCursor<List<?>> queryCursor = cache.query(fieldsQuery)) {
            for (List<?> item : queryCursor) {
                if (preparedQuery.isDtoProjection()) {
                    RuntimePersistentEntity<T> persistentEntity = getEntity(preparedQuery.getRootEntity());
                    IgniteDTOMapper<T, R> introspectedDataMapper = new IgniteDTOMapper<T, R>(persistentEntity, queryCursor, jsonCodec);
                    return introspectedDataMapper.map(item, resultType);
                } else {
                    return (R) item.get(0);
                }
            }
        }
        return null;
    }


    @Override
    public <T, R> boolean exists(@NonNull PreparedQuery<T, R> preparedQuery) {
        final AnnotationMetadata metadata = preparedQuery.getAnnotationMetadata();
        IgniteCache<T, R> cache = resolveCache(metadata);

        String query = preparedQuery.getQuery();
        SqlFieldsQuery fieldsQuery = new SqlFieldsQuery(query);
        try (FieldsQueryCursor<List<?>> cursor = cache.query(fieldsQuery)) {
            boolean hasNext = cursor.iterator().hasNext();
            cursor.close();
            return hasNext;
        }
    }


    @NonNull
    @Override
    public <T> Iterable<T> findAll(@NonNull PagedQuery<T> query) {
        throw new UnsupportedOperationException("The findAll method without an explicit query is not supported. Use findAll(PreparedQuery) instead");
    }

    @Override
    public <T> long count(PagedQuery<T> pagedQuery) {
        throw new UnsupportedOperationException("The findStream method without an explicit query is not supported. Use findStream(PreparedQuery) instead");
    }

    @NonNull
    @Override
    public <T, R> Iterable<R> findAll(@NonNull PreparedQuery<T, R> preparedQuery) {
        return findStream(preparedQuery).collect(Collectors.toList());
    }

    @NonNull
    @Override
    public <T, R> Stream<R> findStream(@NonNull PreparedQuery<T, R> preparedQuery) {
        final AnnotationMetadata metadata = preparedQuery.getAnnotationMetadata();
        IgniteCache<T, R> cache = resolveCache(metadata);

        SqlFieldsQuery fieldsQuery = prepareStatement(
            preparedQuery.getQuery(),
            preparedQuery.getIndexedParameterBinding(),
            preparedQuery.getParameterArray());
        Class<T> rootEntity = preparedQuery.getRootEntity();
        Class<R> resultType = preparedQuery.getResultType();
        if (preparedQuery.isDtoProjection()) {
            try (FieldsQueryCursor<List<?>> cursor = cache.query(fieldsQuery)) {
                return StreamSupport.stream(cursor.spliterator(), false).map((item) -> {
                    if (preparedQuery.isDtoProjection()) {
                        RuntimePersistentEntity<T> persistentEntity = getEntity(preparedQuery.getRootEntity());
                        IgniteDTOMapper<T, R> introspectedDataMapper = new IgniteDTOMapper<>(persistentEntity, cursor, jsonCodec);
                        return introspectedDataMapper.map(item, resultType);
                    } else {
                        return (R) item.get(0);
                    }
                });
            }
        }
        throw new IllegalStateException("Unsupported Entity");
    }

    @NonNull
    @Override
    public <T> Stream<T> findStream(@NonNull PagedQuery<T> query) {
        throw new UnsupportedOperationException("The findStream method without an explicit query is not supported. Use findStream(PreparedQuery) instead");
    }

    @Override
    public <R> Page<R> findPage(@NonNull PagedQuery<R> query) {
        throw new UnsupportedOperationException("The findStream method without an explicit query is not supported. Use findStream(PreparedQuery) instead");
    }

    @NonNull
    @Override
    public <T> T persist(@NonNull InsertOperation<T> operation) {
        AnnotationMetadata metadata = operation.getAnnotationMetadata();
        StoredInsert<T> insert = resolveInsert(operation);
        final Class<?> repositoryType = operation.getRepositoryType();
        T entity = operation.getEntity();
        IgniteCache cache = resolveCache(metadata);
        return persistOne(cache, metadata, repositoryType, insert, entity, new HashSet(5));
    }

    private <T> T persistOne(
        IgniteCache cache,
        AnnotationMetadata metadata,
        Class<?> repositoryType,
        StoredInsert<T> insert,
        T entity,
        Set persisted
    ) {
        String insertSql = insert.getSql();
        if (QUERY_LOG.isDebugEnabled()) {
            QUERY_LOG.debug("Executing SQL Insert: {}", insertSql);
        }
        SqlFieldsQuery fieldsQuery = new SqlFieldsQuery(insertSql);
        setInsertParameters(insert, entity, fieldsQuery);
        cache.query(fieldsQuery);
        persisted.add(entity);
        return entity;
    }

    protected final <T> void setInsertParameters(@NonNull StoredInsert<T> insert, @NonNull T entity, SqlFieldsQuery query) {
        Object now = null;
        RuntimePersistentEntity<T> persistentEntity = insert.getPersistentEntity();
        final String[] parametersBindings = insert.getParameterBinding();
        final Object[] bindings = new Object[parametersBindings.length];

        for (int i = 0; i < parametersBindings.length; i++) {
            String path = parametersBindings[i];
            RuntimePersistentProperty<T> prop = persistentEntity.getPropertyByName(path);
            if (prop == null) {
                int j = path.indexOf('.');
                if (j > -1) {
                    RuntimePersistentProperty embeddedProp = (RuntimePersistentProperty)
                        persistentEntity.getPropertyByPath(path).orElse(null);
                    if (embeddedProp != null) {

                        // embedded case
                        prop = persistentEntity.getPropertyByName(path.substring(0, j));
                        if (prop instanceof Association) {
                            Association assoc = (Association) prop;
                            if (assoc.getKind() == Relation.Kind.EMBEDDED) {

                                Object value = prop.getProperty().get(entity);
                                int index = i + 1;

                                RuntimePersistentEntity<?> embeddedEntity = entities.get(embeddedProp.getProperty().getType());
                                //TODO: work out embedded parameters

//                                if (embeddedEntity != null) {
//                                    Object bean = embeddedProp.getProperty().get(value);
//                                    RuntimePersistentProperty embeddedIdentity = embeddedEntity.getIdentity();
//                                    Object beanId = embeddedIdentity.getProperty().get(bean);
//                                    preparedStatementWriter.setDynamic(
//                                        stmt,
//                                        index,
//                                        embeddedIdentity.getDataType(),
//                                        beanId
//                                    );
//                                } else {
//                                    Object embeddedValue = value != null ? embeddedProp.getProperty().get(value) : null;
//                                    preparedStatementWriter.setDynamic(
//                                        stmt,
//                                        index,
//                                        embeddedProp.getDataType(),
//                                        embeddedValue
//                                    );
//                                }
                            }
                        }
                    }
                }
            } else {
                DataType type = prop.getDataType();
                BeanProperty<T, Object> beanProperty = (BeanProperty<T, Object>) prop.getProperty();
                Object value = beanProperty.get(entity);

                if (prop instanceof Association) {
                    Association association = (Association) prop;
                    if (!association.isForeignKey()) {
                        @SuppressWarnings("unchecked")
                        RuntimePersistentEntity<Object> associatedEntity = (RuntimePersistentEntity<Object>) association.getAssociatedEntity();
                        RuntimePersistentProperty<Object> identity = associatedEntity.getIdentity();
                        if (identity == null) {
                            throw new IllegalArgumentException("Associated entity has not ID: " + associatedEntity.getName());
                        } else {
                            type = identity.getDataType();
                        }
                        BeanProperty<Object, ?> identityProperty = identity.getProperty();
                        if (value != null) {
                            value = identityProperty.get(value);
                        }
                        if (DataSettings.QUERY_LOG.isTraceEnabled()) {
                            DataSettings.QUERY_LOG.trace("Binding value {} to parameter at position: {}", value, i);
                        }
                        bindings[i] = value;
                    }

                } else {
                    if (beanProperty.hasStereotype(AutoPopulated.class)) {
                        if (beanProperty.hasAnnotation(DateCreated.class)) {
                            now = now != null ? now : dateTimeProvider.getNow();
                            if (DataSettings.QUERY_LOG.isTraceEnabled()) {
                                DataSettings.QUERY_LOG.trace("Binding value {} to parameter at position: {}", now, i);
                            }
                            bindings[i] = now;
                            beanProperty.convertAndSet(entity, now);
                        } else if (beanProperty.hasAnnotation(DateUpdated.class)) {
                            now = now != null ? now : dateTimeProvider.getNow();
                            if (DataSettings.QUERY_LOG.isTraceEnabled()) {
                                DataSettings.QUERY_LOG.trace("Binding value {} to parameter at position: {}", now, i);
                            }
                            bindings[i] = now;
                            beanProperty.convertAndSet(entity, now);
                        } else if (UUID.class.isAssignableFrom(beanProperty.getType())) {
                            UUID uuid = UUID.randomUUID();
                            if (DataSettings.QUERY_LOG.isTraceEnabled()) {
                                DataSettings.QUERY_LOG.trace("Binding value {} to parameter at position: {}", uuid, i);
                            }
                            bindings[i] = uuid;
//                                if (dialect.requiresStringUUID(type)) {
//                                    preparedStatementWriter.setString(
//                                        stmt,
//                                        index,
//                                        uuid.toString()
//                                    );
//                                } else {
//                                    preparedStatementWriter.setDynamic(
//                                        stmt,
//                                        index,
//                                        type,
//                                        uuid
//                                    );
//                                }
                            beanProperty.set(entity, uuid);
                        } else {
                            throw new DataAccessException("Unsupported auto-populated annotation type: " + beanProperty.getAnnotationTypeByStereotype(AutoPopulated.class).orElse(null));
                        }

                    } else {
                        if (DataSettings.QUERY_LOG.isTraceEnabled()) {
                            DataSettings.QUERY_LOG.trace("Binding value {} to parameter at position: {}", value, i);
                        }
                        if (type == DataType.JSON && jsonCodec != null) {
                            value = new String(jsonCodec.encode(value), StandardCharsets.UTF_8);
                        }
                        bindings[i] = value;
//                            if (value != null && dialect.requiresStringUUID(type)) {
//                                preparedStatementWriter.setString(
//                                    stmt,
//                                    index,
//                                    value.toString()
//                                );
//                            } else {
//                                preparedStatementWriter.setDynamic(
//                                    stmt,
//                                    index,
//                                    type,
//                                    value
//                                );
//                            }
                    }

                }

            }
        }
        query.setArgs(bindings);
    }

    @NonNull
    @Override
    public <T> T update(@NonNull UpdateOperation<T> operation) {
        final AnnotationMetadata metadata = operation.getAnnotationMetadata();
        final IgniteCache cache = resolveCache(metadata);

        return null;
    }

    @NonNull
    @Override
    public <T> Iterable<T> persistAll(@NonNull BatchOperation<T> operation) {
        //TODO: setup batch.
        //final AnnotationMetadata metadata = operation.getAnnotationMetadata();
        //StoredInsert<T> insert = resolveInsert(operation);
        List<T> results = new ArrayList<>();
        for (T entity : operation) {
            results.add(persist(new InsertOperation<T>() {
                @NonNull
                @Override
                public T getEntity() {
                    return entity;
                }

                @NonNull
                @Override
                public Class<T> getRootEntity() {
                    return operation.getRootEntity();
                }

                @NonNull
                @Override
                public Class<?> getRepositoryType() {
                    return operation.getRepositoryType();
                }

                @NonNull
                @Override
                public String getName() {
                    return operation.getName();
                }

                @NonNull
                @Override
                public AnnotationMetadata getAnnotationMetadata() {
                    return operation.getAnnotationMetadata();
                }
            }));
        }
        return results;
    }

    @NonNull
    @Override
    public Optional<Number> executeUpdate(@NonNull PreparedQuery<?, Number> preparedQuery) {
        final AnnotationMetadata metadata = preparedQuery.getAnnotationMetadata();
        final IgniteCache cache = resolveCache(metadata);

        return Optional.empty();
    }

    @Override
    public <T> Optional<Number> deleteAll(@NonNull BatchOperation<T> operation) {
        final AnnotationMetadata metadata = operation.getAnnotationMetadata();
        final IgniteCache cache = resolveCache(metadata);

        return Optional.empty();
    }


    protected <T> StoredInsert<T> resolveInsert(
        EntityOperation<T> operation
    ) {
        AnnotationMetadata annotationMetadata = operation.getAnnotationMetadata();
        String insertStatement = annotationMetadata.stringValue(Query.class).orElse(null);
        if (insertStatement == null) {
            throw new IllegalStateException("No insert statement present in repository. Ensure it extends GenericRepository and is annotated with @JdbcRepository");
        }
        RuntimePersistentEntity<T> persistentEntity = getEntity(operation.getRootEntity());
        String[] parameterBinding = annotationMetadata.stringValues(DataMethod.class, DataMethod.META_MEMBER_PARAMETER_BINDING_PATHS);
        return new StoredInsert<>(
            insertStatement,
            persistentEntity,
            parameterBinding
        );
    }

    private class QueryKey {
        final Class repositoryType;
        final Class entityType;

        public QueryKey(Class repositoryType, Class entityType) {
            this.repositoryType = repositoryType;
            this.entityType = entityType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            QueryKey queryKey = (QueryKey) o;
            return Objects.equals(repositoryType, queryKey.repositoryType) &&
                Objects.equals(entityType, queryKey.entityType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(repositoryType, entityType);
        }
    }

    /**
     * A stored insert statement.
     *
     * @param <T> The entity type
     */
    protected final class StoredInsert<T> {
        private final String[] parameterBinding;
        private final RuntimePersistentProperty identity;
        private final boolean generateId;
        private final String sql;
        private final RuntimePersistentEntity<T> persistentEntity;
        /**
         * Default constructor.
         *
         * @param sql              The SQL INSERT
         * @param persistentEntity The entity
         * @param parameterBinding The parameter binding
         */
        StoredInsert(
            String sql,
            RuntimePersistentEntity<T> persistentEntity,
            String[] parameterBinding) {
            this.sql = sql;
            this.persistentEntity = persistentEntity;
            this.parameterBinding = parameterBinding;
            this.identity = persistentEntity.getIdentity();
            this.generateId = identity != null && identity.isGenerated();
        }

        /**
         * @return The persistent entity
         */
        public RuntimePersistentEntity<T> getPersistentEntity() {
            return persistentEntity;
        }

        /**
         * @return The SQL
         */
        public @NonNull
        String getSql() {
            return sql;
        }

        /**
         * @return The parameter binding
         */
        public @NonNull
        String[] getParameterBinding() {
            return parameterBinding;
        }

        /**
         * @return The identity
         */
        public @Nullable
        BeanProperty<T, Object> getIdentityProperty() {
            if (identity != null) {
                return identity.getProperty();
            }
            return null;
        }

        /**
         * @return The runtime persistent property.
         */
        RuntimePersistentProperty getIdentity() {
            return identity;
        }

        /**
         * @return Is the id generated
         */
        public boolean isGenerateId() {
            return generateId;
        }
    }

}
