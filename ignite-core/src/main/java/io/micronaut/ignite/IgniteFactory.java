/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.ignite;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.ignite.configuration.DefaultCacheConfiguration;
import io.micronaut.ignite.configuration.DefaultIgniteConfiguration;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;

/**
 * Factory for the implementation of {@link Ignite}.
 */
@Factory
public class IgniteFactory implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(IgniteFactory.class);

    /**
     * The Ignite Configuration.
     *
     * @param configuration the configuration
     * @param cacheConfigurations cache configurations
     * @return Ignite Configuration
     */
    @Bean
    @Named("default")
    @Primary
    public IgniteConfiguration igniteConfiguration(DefaultIgniteConfiguration configuration, Collection<DefaultCacheConfiguration> cacheConfigurations) {
        configuration.setCacheConfiguration(cacheConfigurations.toArray(new CacheConfiguration[0]));
        return configuration;
    }

    /**
     * Create {@link Ignite} instance from {@link IgniteConfiguration}.
     *
     * @param configuration ignite configuration
     * @return create ignite instance
     */
    @Singleton
    @EachBean(IgniteConfiguration.class)
    @Bean(preDestroy = "close")
    public Ignite ignite(IgniteConfiguration configuration) {
        try {
            return Ignition.start(configuration);
        } catch (Exception e) {
            LOG.error("Failed to instantiate Ignite Client: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Stop all instances of Ignite.
     */
    @Override
    public void close() {
        Ignition.stopAll(true);
    }
}
