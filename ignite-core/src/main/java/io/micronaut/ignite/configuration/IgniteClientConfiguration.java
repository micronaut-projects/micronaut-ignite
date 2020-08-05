package io.micronaut.ignite.configuration;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.naming.Named;

/**
 * Ignite cache configuration.
 */
@ConfigurationProperties(value = IgniteClientConfiguration.PREFIX)
public class IgniteClientConfiguration implements Named {
    public static final String PREFIX = "ignite";

    private final String name;
    private String path;

    /**
     * @param name Name or key of the client.
     */
    public IgniteClientConfiguration(@Parameter String name) {
        this.name = name;
    }

    /**
     * path to load in bean configuration.
     *
     * @param path bean config to load.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * path to load in bean configuration.
     *
     * @return bean config to load.
     */
    public String getPath() {
        return path;
    }

    /**
     * @return name or key for client
     */
    @NonNull
    @Override
    public String getName() {
        return this.name;
    }
}
