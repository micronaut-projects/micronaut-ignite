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
package io.micronaut.ignite.configuration;

import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.naming.Named;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.configuration.ClientTransactionConfiguration;

import javax.annotation.Nonnull;

/**
 * Configuration class for an Ignite thin client
 *
 * @author Michael Pollind
 */
@EachProperty(value = IgniteThinClientConfiguration.PREFIX, primary = "default")
public class IgniteThinClientConfiguration extends IgniteAbstractConfiguration implements Named {
    public static final String PREFIX = IgniteAbstractConfiguration.PREFIX + "." + "thin-clients";

    private final String name;

    @ConfigurationBuilder(excludes = {"transactionConfiguration", "binaryConfiguration", "sslContextFactory"})
    private final ClientConfiguration configuration = new ClientConfiguration();

    @ConfigurationBuilder(value = "transactionConfiguration")
    private final ClientTransactionConfiguration transaction = configuration.getTransactionConfiguration();

    /**
     * @param name Name or key of the client.
     */
    public IgniteThinClientConfiguration(@Parameter String name) {
        this.name = name;
    }

    public final ClientTransactionConfiguration getTransaction() {
        return transaction;
    }

    public final ClientConfiguration getConfiguration() {
        return configuration;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }
}
