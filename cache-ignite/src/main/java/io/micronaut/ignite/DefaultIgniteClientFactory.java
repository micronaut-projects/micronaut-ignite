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
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

import javax.inject.Singleton;

/**
 * Factory for the default {@link IgniteClient}.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Requires(beans = ClientConfiguration.class)
@Factory
public class DefaultIgniteClientFactory {

    /**
     * Create the client.
     *
     * @param config the client configuration
     * @return client
     */

    @Bean(preDestroy = "close")
    @Singleton
    public IgniteClient igniteClient(ClientConfiguration config) {
        return Ignition.startClient(config);
    }

}
