package io.micronaut.ignite.configuration;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.naming.Named;

import javax.annotation.Nonnull;

@EachProperty(value = ClientConfiguration.PREFIX)
public class ClientConfiguration implements Named {
    public static final String PREFIX = IgniteConfig.PREFIX + "." + "client";

    private final String name;

    public String path;

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public ClientConfiguration(@Parameter String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }
}
