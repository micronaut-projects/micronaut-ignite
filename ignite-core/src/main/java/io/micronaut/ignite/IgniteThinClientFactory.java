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
import io.micronaut.context.annotation.Requires;
import io.micronaut.ignite.annotation.IgnitePrimary;
import io.micronaut.ignite.configuration.DefaultIgniteThinClientConfiguration;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for the implementation of {@link IgniteClient}.
 */
@Factory
public class IgniteThinClientFactory implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(IgniteThinClientFactory.class);
    private List<IgniteClient> instances = new ArrayList<>(2);

    /**
     * Ignite {@link ClientConfiguration}.
     * @param clientConfiguration client configuration
     * @return client configuration
     */
    @Bean
    @Named("default")
    @Primary
    @Requires(beans = DefaultIgniteThinClientConfiguration.class)
    public ClientConfiguration igniteClientConfiguration(@IgnitePrimary DefaultIgniteThinClientConfiguration clientConfiguration) {
        return clientConfiguration.getConfiguration();
    }

    /**
     *
     * @param configuration client configuration
     * @return Ignite Thin client
     */
    @EachBean(ClientConfiguration.class)
    @Singleton
    @Bean(preDestroy = "close")
    public IgniteClient igniteThinClient(ClientConfiguration configuration) {
        try {
            IgniteClient client = Ignition.startClient(configuration);
            instances.add(client);
            return client;
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
        for (IgniteClient client : instances) {
            try {
                client.close();
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(String.format("Error closing ignite node [%s]: %s", client, e.getMessage()), e);
                }
            }
        }
    }
}
