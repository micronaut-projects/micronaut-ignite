package io.micronaut.ignite.configuration;

import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.ignite.annotation.IgnitePrimary;
import org.apache.ignite.cache.CacheKeyConfiguration;

@IgnitePrimary
@EachProperty(value = DefaultCacheKeyConfiguration.PREFIX, list = true)
public class DefaultCacheKeyConfiguration extends CacheKeyConfiguration {
    public static final String PREFIX = DefaultIgniteConfiguration.PREFIX + "." + "cache-key-configuration";

    @ConfigurationInject
    public DefaultCacheKeyConfiguration(@Bindable(value = "keyCls", defaultValue = "java.lang.Object") Class<?> clazz) {
        super(clazz);
    }
}
