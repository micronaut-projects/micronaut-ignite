package io.micronaut.ignite.annotation;

import io.micronaut.aop.Around;
import io.micronaut.context.annotation.Type;
import io.micronaut.ignite.intercept.IgniteTransactionInterceptor;
import jdk.internal.joptsimple.internal.Strings;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * wraps a call in an ignite transaction
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Around
@Type(IgniteTransactionInterceptor.class)
public @interface IgniteTransaction {

    String client() default "default";
}
