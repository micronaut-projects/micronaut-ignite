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
import io.micronaut.core.beans.BeanWrapper;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.annotation.TypeRole;
import io.micronaut.data.annotation.sql.SqlMembers;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.query.QueryModel;
import io.micronaut.data.model.query.QueryParameter;
import io.micronaut.data.model.query.builder.QueryResult;
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
import org.slf4j.Logger;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementation of the {@link RepositoryOperations} interface for Ignite.
 */
@EachBean(Ignite.class)
public class IgniteRepositoryOperations implements RepositoryOperations {

    private static final Object IGNORED_PARAMETER = new Object();
    protected static final Logger QUERY_LOG = DataSettings.QUERY_LOG;
    protected static final IgniteSqlQueryBuilder DEFAULT_IGNITE_QUERY_BUILDER = new IgniteSqlQueryBuilder();
    protected static final Pattern IN_EXPRESSION_PATTERN = Pattern.compile("\\s\\?\\$IN\\((\\d+)\\)");
    protected static final String NOT_TRUE_EXPRESSION = "1 = 2";

    private final Ignite ignite;
    private final BeanContext beanContext;
    protected final MediaTypeCodec jsonCodec;
    protected final DateTimeProvider dateTimeProvider;
    private final ConversionService conversionService;

    private final Map<Class, RuntimePersistentEntity> entities = new ConcurrentHashMap<>(10);
    private final Map<Class, RuntimePersistentProperty> idReaders = new ConcurrentHashMap<>(10);
    private final Map<QueryKey, StoredInsert> entityInserts = new ConcurrentHashMap<>(10);
    private final Map<QueryKey, StoredInsert> entityUpdates = new ConcurrentHashMap<>(10);

    public IgniteRepositoryOperations(Ignite ignite,
                                      List<MediaTypeCodec> codecs,
                                      BeanContext beanContext,
                                      @NonNull DateTimeProvider dateTimeProvider,
                                      ConversionService conversionService) {
        this.dateTimeProvider = dateTimeProvider;
        this.ignite = ignite;
        this.beanContext = beanContext;
        this.jsonCodec = resolveJsonCodec(codecs);
        this.conversionService  = conversionService;
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

//    public <T, R> SqlFieldsQuery prepareStatement(String query, int[] bindings, Object[] targets) {
//        SqlFieldsQuery fieldsQuery = new SqlFieldsQuery(query);
//        Object[] args = new Object[bindings.length];
//        for (int i = 0; i < bindings.length; i++) {
//            int parameterIndex = bindings[i];
//            Object value = targets[parameterIndex];
//            args[i] = value;
//        }
//        fieldsQuery.setArgs(args);
//        return fieldsQuery;
//    }


    /**
     * Compute the size of the given object.
     * @param value The value
     * @return The size
     */
    protected final int sizeOf(Object value) {
        if (value instanceof Collection) {
            return ((Collection) value).size();
        } else if (value instanceof Iterable) {
            int i = 0;
            for (Object ignored : ((Iterable) value)) {
                i++;
            }
            return i;
        } else if (value.getClass().isArray()) {
            return Array.getLength(value);
        }
        return 1;
    }


    public <T, R> SqlFieldsQuery prepareStatement(PreparedQuery<T,R> preparedQuery, boolean isUpdate, boolean isSingleResult) {
        Object[] queryParameters = preparedQuery.getParameterArray();
        int[] parameterBinding = preparedQuery.getIndexedParameterBinding();
        DataType[] parameterTypes = preparedQuery.getIndexedParameterTypes();
        String query = preparedQuery.getQuery();
        final boolean hasIn = preparedQuery.hasInExpression();
        AnnotationMetadata metadata = preparedQuery.getAnnotationMetadata();
        if (hasIn) {
            Matcher matcher = IN_EXPRESSION_PATTERN.matcher(query);
            // this has to be done is two passes, one to remove and establish new indexes
            // and again to expand existing indexes
            while (matcher.find()) {
                int inIndex = Integer.valueOf(matcher.group(1));
                int queryParameterIndex = parameterBinding[inIndex - 1];
                Object value = queryParameters[queryParameterIndex];

                if (value == null) {
                    query = matcher.replaceFirst(NOT_TRUE_EXPRESSION);
                    queryParameters[queryParameterIndex] = IGNORED_PARAMETER;
                } else {
                    int size = sizeOf(value);
                    if (size == 0) {
                        queryParameters[queryParameterIndex] = IGNORED_PARAMETER;
                        query = matcher.replaceFirst(NOT_TRUE_EXPRESSION);
                    } else {
                        String replacement = " IN(" + String.join(",", Collections.nCopies(size, "?")) + ")";
                        query = matcher.replaceFirst(replacement);
                    }
                }
                matcher = IN_EXPRESSION_PATTERN.matcher(query);
            }
        }

        if (!isUpdate) {
            Pageable pageable = preparedQuery.getPageable();
            if (pageable != Pageable.UNPAGED) {
                Class<T> rootEntity = preparedQuery.getRootEntity();
                Sort sort = pageable.getSort();
                if (sort.isSorted()) {
                    query += DEFAULT_IGNITE_QUERY_BUILDER.buildOrderBy(getEntity(rootEntity), sort).getQuery();
                }
                if (isSingleResult && pageable.getOffset() > 0) {
                    pageable = Pageable.from(pageable.getNumber(), 1);
                }
                query += DEFAULT_IGNITE_QUERY_BUILDER.buildPagination(pageable).getQuery();
            }
        }

        if (QUERY_LOG.isDebugEnabled()) {
            QUERY_LOG.debug("Executing Query: {}", query);
        }
        SqlFieldsQuery fieldsQuery = new SqlFieldsQuery(query);
        List<Object> bindings = new ArrayList<>(parameterBinding.length);
        int index = 0;
        for (int i = 0; i < parameterBinding.length; i++) {
            int parameterIndex = parameterBinding[i];
            DataType dataType = parameterTypes[i];
            Object value;
            if (parameterIndex > -1) {
                value = queryParameters[parameterIndex];
            } else {
                String[] indexedParameterPaths = preparedQuery.getIndexedParameterPaths();
                String propertyPath = indexedParameterPaths[i];
                if (propertyPath != null) {

                    String lastUpdatedProperty = preparedQuery.getLastUpdatedProperty();
                    if (lastUpdatedProperty != null && lastUpdatedProperty.equals(propertyPath)) {
                        Class<?> lastUpdatedType = preparedQuery.getLastUpdatedType();
                        if (lastUpdatedType == null) {
                            throw new IllegalStateException("Could not establish last updated time for entity: " + preparedQuery.getRootEntity());
                        }
                        Object timestamp = ConversionService.SHARED.convert(dateTimeProvider.getNow(), lastUpdatedType).orElse(null);
                        if (timestamp == null) {
                            throw new IllegalStateException("Unsupported date type: " + lastUpdatedType);
                        }
                        value = timestamp;
                    } else {
                        int j = propertyPath.indexOf('.');
                        if (j > -1) {
                            String subProp = propertyPath.substring(j + 1);
                            value = queryParameters[Integer.valueOf(propertyPath.substring(0, j))];
                            value = BeanWrapper.getWrapper(value).getRequiredProperty(subProp, Argument.OBJECT_ARGUMENT);
                        } else {
                            throw new IllegalStateException("Invalid query [" + query + "]. Unable to establish parameter value for parameter at position: " + (i + 1));
                        }
                    }
                } else {
                    throw new IllegalStateException("Invalid query [" + query + "]. Unable to establish parameter value for parameter at position: " + (i + 1));
                }
            }

            if (QUERY_LOG.isTraceEnabled()) {
                QUERY_LOG.trace("Binding parameter at position {} to value {}", index, value);
            }
            if (value == null) {
                bindings.add(null);
            } else if (value != IGNORED_PARAMETER) {
                if (value instanceof Iterable) {
                    Iterable iter = (Iterable) value;
                    for (Object o : iter) {
                        bindings.add(o);
                    }
                } else if (value.getClass().isArray()) {
                    if (value instanceof byte[]) {
                        bindings.add(value);
                    } else {
                        int len = Array.getLength(value);
                        for (int j = 0; j < len; j++) {
                            Object o = Array.get(value, j);
                            bindings.add(o);
                        }
                    }
                } else {
                    bindings.add(value);
                }
            }
        }
        fieldsQuery.setArgs(bindings);
        return fieldsQuery;
    }


    @Nullable
    @Override
    public <T, R> R findOne(@NonNull PreparedQuery<T, R> preparedQuery) {
        final Class<R> resultType = preparedQuery.getResultType();
        final AnnotationMetadata annotationMetadata = preparedQuery.getAnnotationMetadata();
        final IgniteCache<T, R> cache = resolveCache(annotationMetadata);
        SqlFieldsQuery fieldsQuery = prepareStatement(preparedQuery, false, true);
        try (FieldsQueryCursor<List<?>> cursor =  cache.query(fieldsQuery)){
            for(List<?> target: cursor) {


            }
        }


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
        SqlFieldsQuery fieldsQuery = prepareStatement(preparedQuery, false, true);
        try (FieldsQueryCursor<List<?>> cursor = cache.query(fieldsQuery)) {
            return cursor.iterator().hasNext();
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
        SqlFieldsQuery fieldsQuery = prepareStatement(preparedQuery, false,false);

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



    /**
     * Resolves a stored insert for the given entity.
     *
     * @param annotationMetadata The repository annotation metadata
     * @param repositoryType     The repository type
     * @param rootEntity         The root entity
     * @param persistentEntity   The persistent entity
     * @param <T>                The generic type
     * @return The insert
     */
    protected @NonNull
    <T> StoredInsert<T> resolveEntityInsert(
        AnnotationMetadata annotationMetadata,
        Class<?> repositoryType,
        @NonNull Class<?> rootEntity,
        @NonNull RuntimePersistentEntity<?> persistentEntity) {

        //noinspection unchecked
        return entityInserts.computeIfAbsent(new QueryKey(repositoryType, rootEntity), (queryKey) -> {
            final QueryResult queryResult = DEFAULT_IGNITE_QUERY_BUILDER.buildInsert(annotationMetadata, persistentEntity);

            final String sql = queryResult.getQuery();
            final Map<String, String> parameters = queryResult.getParameters();
            return new StoredInsert<>(
                sql,
                persistentEntity,
                parameters.values().toArray(new String[0])
            );
        });
    }

    /**
     * Resolves a stored update for the given entity.
     *
     * @param annotationMetadata The repository annotation metadata
     * @param repositoryType     The repository type
     * @param rootEntity         The root entity
     * @param persistentEntity   The persistent entity
     * @param <T>                The generic type
     * @return The insert
     */
    protected @NonNull
    <T> StoredInsert<T> resolveEntityUpdate(
        AnnotationMetadata annotationMetadata,
        Class<?> repositoryType,
        @NonNull Class<?> rootEntity,
        @NonNull RuntimePersistentEntity<?> persistentEntity) {

        final QueryKey key = new QueryKey(repositoryType, rootEntity);
        //noinspection unchecked
        return entityUpdates.computeIfAbsent(key, (queryKey) -> {

            final String idName;
            final PersistentProperty identity = persistentEntity.getIdentity();
            if (identity != null) {
                idName = identity.getName();
            } else {
                idName = TypeRole.ID;
            }
            final QueryModel queryModel = QueryModel.from(persistentEntity)
                .idEq(new QueryParameter(idName));
            List<String> updateProperties = persistentEntity.getPersistentProperties()
                .stream().filter(p ->
                    !((p instanceof Association) && ((Association) p).isForeignKey()) &&
                        p.getAnnotationMetadata().booleanValue(AutoPopulated.class, "updateable").orElse(true)
                )
                .map(PersistentProperty::getName)
                .collect(Collectors.toList());
            final QueryResult queryResult = DEFAULT_IGNITE_QUERY_BUILDER.buildUpdate(
                annotationMetadata,
                queryModel,
                updateProperties
            );

            final String sql = queryResult.getQuery();
            final Map<String, String> parameters = queryResult.getParameters();
            return new StoredInsert<>(
                sql,
                persistentEntity,
                parameters.values().toArray(new String[0])
            );
        });
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
        return persistOne(metadata, repositoryType, insert, entity, new HashSet(5));
    }

    private <T> T persistOne(
        AnnotationMetadata metadata,
        Class<?> repositoryType,
        StoredInsert<T> insert,
        T entity,
        Set persisted
    ) {
        IgniteCache cache = resolveCache(metadata);
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
                                RuntimePersistentEntity<?> embeddedEntity = entities.get(embeddedProp.getProperty().getType());

                                if (embeddedEntity != null) {
                                    Object bean = embeddedProp.getProperty().get(value);
                                    RuntimePersistentProperty embeddedIdentity = embeddedEntity.getIdentity();
                                    Object beanId = embeddedIdentity.getProperty().get(bean);
                                    bindings[i] = beanId;
                                } else {
                                    Object embeddedValue = value != null ? embeddedProp.getProperty().get(value) : null;
                                    bindings[i] = embeddedValue;
                                }
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

    private <T> T updateOne(
        AnnotationMetadata annotationMetadata,
        Class<?> repositoryType,
        String query,
        String[] params,
        T entity,
        Set persisted) {

        Objects.requireNonNull(entity, "Passed entity cannot be null");
        IgniteCache cache = resolveCache(annotationMetadata);
        if (StringUtils.isNotEmpty(query) && ArrayUtils.isNotEmpty(params)) {
            final RuntimePersistentEntity<T> persistentEntity =
                (RuntimePersistentEntity<T>) getEntity(entity.getClass());

            if (QUERY_LOG.isDebugEnabled()) {
                QUERY_LOG.debug("Executing SQL UPDATE: {}", query);
            }

            final Object[] bindings = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                String propertyName = params[i];
                RuntimePersistentProperty<T> pp =
                    persistentEntity.getPropertyByName(propertyName);
                if (pp == null) {
                    int j = propertyName.indexOf('.');
                    if (j > -1) {
                        RuntimePersistentProperty embeddedProp = (RuntimePersistentProperty)
                            persistentEntity.getPropertyByPath(propertyName).orElse(null);
                        if (embeddedProp != null) {

                            pp = persistentEntity.getPropertyByName(propertyName.substring(0, j));
                            if (pp instanceof Association) {
                                Association assoc = (Association) pp;
                                if (assoc.getKind() == Relation.Kind.EMBEDDED) {
                                    Object embeddedInstance = pp.getProperty().get(entity);

                                    Object embeddedValue = embeddedInstance != null ? embeddedProp.getProperty().get(embeddedInstance) : null;
                                    bindings[i] = embeddedValue;
                                }
                            }
                        } else {
                            throw new IllegalStateException("Cannot perform update for non-existent property: " + persistentEntity.getSimpleName() + "." + propertyName);
                        }
                    } else {
                        throw new IllegalStateException("Cannot perform update for non-existent property: " + persistentEntity.getSimpleName() + "." + propertyName);
                    }
                } else {

                    final Object newValue;
                    final BeanProperty<T, ?> beanProperty = pp.getProperty();
                    if (beanProperty.hasAnnotation(DateUpdated.class)) {
                        newValue = dateTimeProvider.getNow();
                        beanProperty.convertAndSet(entity, newValue);
                    } else {
                        newValue = beanProperty.get(entity);
                    }
                    final DataType dataType = pp.getDataType();
                    if (dataType == DataType.ENTITY && newValue != null && pp instanceof Association) {
                        final RuntimePersistentProperty<Object> idReader = getIdReader(newValue);
                        final Association association = (Association) pp;
                        final BeanProperty<Object, ?> idReaderProperty = idReader.getProperty();
                        final Object id = idReaderProperty.get(newValue);
                        if (QUERY_LOG.isTraceEnabled()) {
                            QUERY_LOG.trace("Binding parameter at position {} to value {}", i + 1, id);
                        }
                        if (id != null) {
                            bindings[i] = id;
                            if (association.doesCascade(Relation.Cascade.PERSIST) && !persisted.contains(newValue)) {
                                final Relation.Kind kind = association.getKind();
                                final RuntimePersistentEntity associatedEntity = (RuntimePersistentEntity) association.getAssociatedEntity();
                                switch (kind) {
                                    case ONE_TO_ONE:
                                    case MANY_TO_ONE:
                                        persisted.add(newValue);
                                        final StoredInsert<Object> updateStatement = resolveEntityUpdate(
                                            annotationMetadata,
                                            repositoryType,
                                            associatedEntity.getIntrospection().getBeanType(),
                                            associatedEntity
                                        );
                                        updateOne(
                                            annotationMetadata,
                                            repositoryType,
                                            updateStatement.getSql(),
                                            updateStatement.getParameterBinding(),
                                            newValue,
                                            persisted
                                        );
                                        break;
                                    case MANY_TO_MANY:
                                    case ONE_TO_MANY:
                                        // TODO: handle cascading updates to collections?

                                    case EMBEDDED:
                                    default:
                                        // TODO: embedded type updates
                                }
                            }
                        } else {
                            if (association.doesCascade(Relation.Cascade.PERSIST) && !persisted.contains(newValue)) {
                                final RuntimePersistentEntity associatedEntity = (RuntimePersistentEntity) association.getAssociatedEntity();

                                StoredInsert associatedInsert = resolveEntityInsert(
                                    annotationMetadata,
                                    repositoryType,
                                    associatedEntity.getIntrospection().getBeanType(),
                                    associatedEntity
                                );
                                persistOne(
                                    annotationMetadata,
                                    repositoryType,
                                    associatedInsert,
                                    newValue,
                                    persisted
                                );
                                final Object assignedId = idReaderProperty.get(newValue);
                                if (assignedId != null) {
                                    bindings[i] = assignedId;
                                }
                            }
                        }
                    } else if (dataType == DataType.JSON && jsonCodec != null) {
                        String value = new String(jsonCodec.encode(newValue), StandardCharsets.UTF_8);
                        if (QUERY_LOG.isTraceEnabled()) {
                            QUERY_LOG.trace("Binding parameter at position {} to value {}", i + 1, value);
                        }
                        bindings[i] = value;
                    } else {
                        if (QUERY_LOG.isTraceEnabled()) {
                            QUERY_LOG.trace("Binding parameter at position {} to value {}", i + 1, newValue);
                        }

                        bindings[i] = newValue;
                    }
                }

                cache.query(new SqlFieldsQuery(query).setArgs(bindings));
            }
        }
        return entity;
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
        throw new UnsupportedOperationException("The deleteAll method via batch is unsupported. Execute the SQL update directly");
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

    /**
     * Obtain an ID reader for the given object.
     *
     * @param o The object
     * @return The ID reader
     */
    @NonNull
    protected final RuntimePersistentProperty<Object> getIdReader(@NonNull Object o) {
        Class<Object> type = (Class<Object>) o.getClass();
        RuntimePersistentProperty beanProperty = idReaders.get(type);
        if (beanProperty == null) {

            RuntimePersistentEntity<Object> entity = getEntity(type);
            RuntimePersistentProperty<Object> identity = entity.getIdentity();
            if (identity == null) {
                throw new DataAccessException("Entity has no ID: " + entity.getName());
            }
            beanProperty = identity;
            idReaders.put(type, beanProperty);
        }
        return beanProperty;
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
