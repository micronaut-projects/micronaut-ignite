package io.micronaut.ignite.annotation;

public @interface Ordered {
    long timeout() default 0;
}
