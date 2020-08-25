package io.micronaut.ignite.docs.config;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.ignite.annotation.IgnitePrimary;
import org.apache.ignite.configuration.CacheConfiguration;

@Factory
public class IgniteCacheConfigurationFactory {
    @Bean
    @IgnitePrimary
    CacheConfiguration<String,String> cacheInstance() {
        return new CacheConfiguration<String, String>("my-cache")
            .setBackups(10);
    }
}
