package io.micronaut.ignite.docs.config

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.ignite.annotation.IgnitePrimary
import org.apache.ignite.configuration.CacheConfiguration

@Factory
class IgniteCacheConfigurationFactory {
    @Bean
    @IgnitePrimary
    fun cacheInstance(): CacheConfiguration<String, String>? {
        return CacheConfiguration<String, String>("my-cache")
            .setBackups(2)
    }
}
