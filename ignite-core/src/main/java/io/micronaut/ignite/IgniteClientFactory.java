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
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.ignite.configuration.IgniteAbstractConfiguration;
import io.micronaut.ignite.configuration.IgniteClientConfiguration;
import io.micronaut.ignite.configuration.IgniteThinClientConfiguration;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.IgniteClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.net.URL;
import java.util.Optional;

/**
 * Factory class that creates {@link Ignite} and {@link IgniteClient}.
 *
 * @author Michael Pollind
 */
@Factory
@Requires(property = IgniteAbstractConfiguration.PREFIX + ".enabled", value = "true", defaultValue = "false")
public class IgniteClientFactory implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(IgniteClientFactory.class);

    private final ResourceResolver resourceResolver;

    public IgniteClientFactory(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    /**
     * Create a singleton {@link Ignite} client, based on an existing {@link IgniteClientConfiguration} bean.
     *
     * @param configuration the configuration read it as a bean
     * @return {@link Ignite}
     */
    @Singleton
    @EachBean(IgniteClientConfiguration.class)
    @Bean(preDestroy = "close")
    public Ignite igniteClient(IgniteClientConfiguration configuration) {
        try {
            Optional<URL> template = resourceResolver.getResource(configuration.getPath());
            if (!template.isPresent()) {
                throw new RuntimeException("failed to find configuration: " + configuration.getPath());
            }
            return Ignition.start(template.get());
        } catch (Exception e) {
            LOG.error("Failed to instantiate Ignite: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Create a singleton {@link IgniteClient} client, based on an existing {@link IgniteThinClientConfiguration} bean.
     *
     * @param configuration the configuration read it as a bean
     * @return {@link IgniteClient}
     */
    @Singleton
    @EachBean(IgniteThinClientConfiguration.class)
    @Bean(preDestroy = "close")
    public IgniteClient igniteThinClient(IgniteThinClientConfiguration configuration) {
        try {
            return Ignition.startClient(configuration.getConfiguration());
        } catch (Exception e) {
            LOG.error("Failed to instantiate Ignite Client: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * stop all ignite instances.
     */
    @Override
    public void close() {
        Ignition.stopAll(true);
    }
}
