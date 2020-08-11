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
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.sql.SqlMembers;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.runtime.BatchOperation;
import io.micronaut.data.model.runtime.InsertOperation;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import io.micronaut.data.model.runtime.UpdateOperation;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.MediaTypeCodec;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementation of the {@link RepositoryOperations} interface for Ignite.
 */
@EachBean(Ignite.class)
public class IgniteRepositoryOperations implements RepositoryOperations {
    private final BeanContext beanContext;
    private final Map<Class, RuntimePersistentEntity> entities = new ConcurrentHashMap<>(10);
    protected final MediaTypeCodec jsonCodec;
    private final Ignite ignite;

    public IgniteRepositoryOperations(Ignite ignite,
                                      List<MediaTypeCodec> codecs,
                                      BeanContext beanContext) {
        this.ignite = ignite;
        this.beanContext = beanContext;
        this.jsonCodec = resolveJsonCodec(codecs);
    }


    private MediaTypeCodec resolveJsonCodec(List<MediaTypeCodec> codecs) {
        return CollectionUtils.isNotEmpty(codecs) ? codecs.stream().filter(c -> c.getMediaTypes().contains(MediaType.APPLICATION_JSON_TYPE)).findFirst().orElse(null) : null;
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

    @Nullable
    @Override
    public <T, R> R findOne(@NonNull PreparedQuery<T, R> preparedQuery) {
        final AnnotationMetadata annotationMetadata = preparedQuery.getAnnotationMetadata();
        IgniteCache<T, R> cache =  null;

        SqlFieldsQuery query = this.prepareStatement(preparedQuery);
        Class<R> resultType = preparedQuery.getResultType();

        try (FieldsQueryCursor<List<?>> queryCursor = cache.query(query)) {
            for (List<?> item : queryCursor) {
                if (preparedQuery.getResultDataType() == DataType.ENTITY) {
                    throw new IllegalStateException("Entity mappings not supported");
                } else {
                    if (preparedQuery.isDtoProjection()) {
                        RuntimePersistentEntity<T> persistentEntity = getEntity(preparedQuery.getRootEntity());
                        IgniteDTOMapper<T, R> introspectedDataMapper = new IgniteDTOMapper<T, R>(persistentEntity, queryCursor, jsonCodec);
                        return introspectedDataMapper.map(item, resultType);
                    } else {
                        return (R) item.get(0);
                    }
                }
            }
        }
        return null;
    }

    public <K,V> IgniteCache<K,V> getCacheInstance(AnnotationMetadata metadata){
        Optional<String> schema = metadata.stringValue(MappedEntity.class, SqlMembers.SCHEMA);
        if(!schema.isPresent())
            throw new IllegalStateException("Mapped Schema not found");
        return ignite.cache(schema.get());
    }

    public <T, R> SqlFieldsQuery prepareStatement(@NonNull PreparedQuery<T, R> preparedQuery) {
        Object[] queryParameters = preparedQuery.getParameterArray();
        AnnotationMetadata metadata = preparedQuery.getAnnotationMetadata();
        int[] parameterBinding = preparedQuery.getIndexedParameterBinding();
        DataType[] parameterTypes = preparedQuery.getIndexedParameterTypes();
        String query = preparedQuery.getQuery();

        preparedQuery.getAnnotationMetadata().stringValue(MappedEntity.class, SqlMembers.SCHEMA).orElse("default");



        SqlFieldsQuery fieldsQuery = new SqlFieldsQuery(query);
        Object[] args = new Object[parameterBinding.length];
        for (int i = 0; i < parameterBinding.length; i++) {
            int parameterIndex = parameterBinding[i];
            Object value = queryParameters[parameterIndex];
            args[i] = value;
        }
        fieldsQuery.setArgs(args);
        return fieldsQuery;
    }

    @Override
    public <T, R> boolean exists(@NonNull PreparedQuery<T, R> preparedQuery) {
        final AnnotationMetadata annotationMetadata = preparedQuery.getAnnotationMetadata();
        IgniteCache<T, R> cache = null;
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
        final AnnotationMetadata annotationMetadata = preparedQuery.getAnnotationMetadata();
        IgniteCache<T, R> cache = null;

        Class<T> rootEntity = preparedQuery.getRootEntity();
        Class<R> resultType = preparedQuery.getResultType();
        SqlFieldsQuery query = this.prepareStatement(preparedQuery);
        if (preparedQuery.isDtoProjection()) {
            try (FieldsQueryCursor<List<?>> cursor = cache.query(query)) {
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

        final Class<?> repositoryType = operation.getRepositoryType();
        final AnnotationMetadata annotationMetadata = operation.getAnnotationMetadata();

        String insertStatement = annotationMetadata.stringValue(Query.class).orElse(null);
        if (insertStatement == null) {
            throw new IllegalStateException("No insert statement present in repository. Ensure it extends GenericRepository and is annotated with @IgniteRepository");
        }
        RuntimePersistentEntity<T> persistentEntity = getEntity(operation.getRootEntity());
        List<String> updateProperties = persistentEntity.getPersistentProperties()
            .stream().filter(p ->
                !((p instanceof Association) && ((Association) p).isForeignKey()) &&
                    p.getAnnotationMetadata().booleanValue(AutoPopulated.class, "updateable").orElse(true)
            )
            .map(PersistentProperty::getName)
            .collect(Collectors.toList());

        SqlFieldsQuery query = new SqlFieldsQuery(insertStatement);
//        Object[] args =
        for (int i = 0; i < updateProperties.size(); i++) {
            RuntimePersistentProperty property = persistentEntity.getPropertyByName(updateProperties.get(i));


        }

        return null;
    }

    @NonNull
    @Override
    public <T> T update(@NonNull UpdateOperation<T> operation) {
        return null;
    }

    @NonNull
    @Override
    public <T> Iterable<T> persistAll(@NonNull BatchOperation<T> operation) {
        return null;
    }

    @NonNull
    @Override
    public Optional<Number> executeUpdate(@NonNull PreparedQuery<?, Number> preparedQuery) {

        return Optional.empty();
    }

    @Override
    public <T> Optional<Number> deleteAll(@NonNull BatchOperation<T> operation) {
        return Optional.empty();
    }
}
