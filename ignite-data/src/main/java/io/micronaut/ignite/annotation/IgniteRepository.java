package io.micronaut.ignite.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.annotation.RepositoryConfiguration;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import io.micronaut.ignite.IgniteRepositoryOperations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@RepositoryConfiguration(
    queryBuilder = SqlQueryBuilder.class,
    operations = IgniteRepositoryOperations.class,
    implicitQueries = false,
    namedParameters = false
)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repository
@IgniteCacheRef(value = "default")
public @interface IgniteRepository {

    @AliasFor(annotation = IgniteCacheRef.class, member = "value")
    String value() default "default";

    @AliasFor(annotation = Repository.class, member = "value")
    @AliasFor(annotation = IgniteCacheRef.class, member = "client")
    String client() default "default";

    @AliasFor(annotation = IgniteCacheRef.class, member = "configurationId")
    String configurationId() default "";
}
