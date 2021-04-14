package io.micronaut.ignite.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Subscription {
    /**
     * The name of the subscription, it could be a simple name such as "animals" or
     * a FQN such as {@code projects/<project_name>/subscriptions/<subscription_name>}.
     * @return the subscription name
     */
    String value();
}
