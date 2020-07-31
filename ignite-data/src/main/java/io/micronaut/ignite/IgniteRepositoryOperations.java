package io.micronaut.ignite;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.runtime.BatchOperation;
import io.micronaut.data.model.runtime.InsertOperation;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.UpdateOperation;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.ignite.annotation.IgniteCacheRef;
import org.apache.ignite.Ignite;
import org.apache.ignite.client.IgniteClient;

import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

@EachBean(IgniteClient.class)
public class IgniteRepositoryOperations implements RepositoryOperations {
    private final BeanContext beanContext;
    private final Ignite ignite;

    public IgniteRepositoryOperations(Ignite ignite, BeanContext beanContext) {
        this.beanContext = beanContext;
        this.ignite = ignite;
    }

    @Nullable
    @Override
    public <T> T findOne(@NonNull Class<T> type, @NonNull Serializable id) {

        return null;
    }

    @Nullable
    @Override
    public <T, R> R findOne(@NonNull PreparedQuery<T, R> preparedQuery) {

        AnnotationMetadata metadata = preparedQuery.getAnnotationMetadata();

        AnnotationValue<IgniteCacheRef> cacheRefAnnotationValue = metadata.getAnnotation(IgniteCacheRef.class);

//        metadata.stringValue(IgniteKey.class,"value").orElseThrow(() -> new IllegalStateException("repository does not have @IgniteKey"));

//        AnnotationValue<IgniteKey> keyAnnotationValue = preparedQuery.findAnnotation(IgniteKey.class).orElseThrow(() -> new IllegalStateException("repository does not have @IgniteKey"));

//        preparedQuery.getAnnotationMetadata().getAnnotation()
//        preparedQuery.getAnnotation()
        return null;
    }

    @Override
    public <T, R> boolean exists(@NonNull PreparedQuery<T, R> preparedQuery) {
        return false;
    }

    @NonNull
    @Override
    public <T> Iterable<T> findAll(@NonNull PagedQuery<T> query) {
        return null;
    }

    @Override
    public <T> long count(PagedQuery<T> pagedQuery) {
        return 0;
    }

    @NonNull
    @Override
    public <T, R> Iterable<R> findAll(@NonNull PreparedQuery<T, R> preparedQuery) {
        return null;
    }

    @NonNull
    @Override
    public <T, R> Stream<R> findStream(@NonNull PreparedQuery<T, R> preparedQuery) {
        return null;
    }

    @NonNull
    @Override
    public <T> Stream<T> findStream(@NonNull PagedQuery<T> query) {
        return null;
    }

    @Override
    public <R> Page<R> findPage(@NonNull PagedQuery<R> query) {
        return null;
    }

    @NonNull
    @Override
    public <T> T persist(@NonNull InsertOperation<T> operation) {
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
