package io.micronaut.ignite.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a pubsub topic to be used by classes annotated with {@link }
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Topic {

    /**
     * Set the name of the topic used to publish messages. Valid names are simple names such as "animals" or
     * FQN names such as {@code projects/<project_name>/topics/<topic_name>}
     * @return The name of the topic to publish messages to
     */
    String value();



}
