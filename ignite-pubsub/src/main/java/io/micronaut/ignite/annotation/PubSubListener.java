package io.micronaut.ignite.annotation;

import io.micronaut.messaging.annotation.MessageListener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@MessageListener
public @interface PubSubListener {
}
