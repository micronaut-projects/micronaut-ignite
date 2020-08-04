package io.micronaut.ignite.annotation;

public @interface IgniteCacheConfiguration {
    /**
     *
     * @return
     */
    String configurationId() default "";

    /**
     *
     * @return
     */
    String nearConfigurationId() default "";
}
