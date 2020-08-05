package io.micronaut.ignite.annotation;

import io.micronaut.context.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@IgniteRef(value = "")
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface IgniteCacheRef {


    @AliasFor(annotation = IgniteRef.class, member = "value")
    String value();

    @AliasFor(annotation = IgniteRef.class, member = "client")
    String client() default "default";

    String nearCacheId() default "";

    String cacheConfigurationId() default "";

    boolean uniqueId() default false;

    boolean create() default false;
}
