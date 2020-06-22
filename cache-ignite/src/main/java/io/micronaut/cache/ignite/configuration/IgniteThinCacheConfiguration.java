package io.micronaut.cache.ignite.configuration;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import org.apache.ignite.client.ClientCacheConfiguration;

/**
 * Ignite cache configuration.
 */
@EachProperty(value = "ignite-thin.caches", primary = "default")
public class IgniteThinCacheConfiguration {
    private final String name;
    private String client = "default";

    @ConfigurationBuilder(excludes = {"name", "keyConfiguration", "queryEntities"})
    private final ClientCacheConfiguration configuration = new ClientCacheConfiguration();

    /**
     * @param name Name or key for client.
     */
    public IgniteThinCacheConfiguration(@Parameter String name) {
        this.name = name;
    }

    /**
     * @param client name of client to reference when building cache.
     */
    public void setClient(String client) {
        this.client = client;
    }

    /**
     * @return name of client to reference when building cache.
     */
    public String getClient() {
        return client;
    }

    /**
     * @return ignite cache configuration.
     */
    public ClientCacheConfiguration getConfiguration() {
        return configuration.setName(this.name);
    }

    /**
     * @return name or key for client.
     */
    @NonNull
    public String getName() {
        return this.name;
    }
}
