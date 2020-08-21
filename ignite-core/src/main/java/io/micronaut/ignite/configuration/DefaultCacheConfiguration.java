package io.micronaut.ignite.configuration;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.naming.Named;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.configuration.CacheConfiguration;
import org.jetbrains.annotations.NotNull;

@EachProperty(value = DefaultIgniteConfiguration.PREFIX + "." + DefaultCacheConfiguration.PREFIX)
public class DefaultCacheConfiguration<K,V> extends CacheConfiguration<K,V> implements Named {
    public static final String PREFIX = "cacheConfigurations";
    private final String name;

    public DefaultCacheConfiguration(@Parameter String name){
        super();
        this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @EachProperty(value = "queryEntities", list = true)
    public static class DefaultQueryEntities extends QueryEntity {
    }
}
