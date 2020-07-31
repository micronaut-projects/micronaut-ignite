package io.micronaut.ignite.config;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;

@EachProperty(value = IgniteDataConfiguration.PREFIX)
public class IgniteDataConfiguration {
    public static final String PREFIX = "ignite.data";

    private final String name;

    public IgniteDataConfiguration(@Parameter String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
