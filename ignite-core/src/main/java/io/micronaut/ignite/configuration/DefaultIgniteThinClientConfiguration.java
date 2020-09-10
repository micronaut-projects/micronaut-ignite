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
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.ignite.annotation.IgnitePrimary;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.configuration.ClientTransactionConfiguration;

/**
 * The thin client configuration.
 */
@IgnitePrimary
@ConfigurationProperties(value = DefaultIgniteThinClientConfiguration.PREFIX)
@Requires(property = DefaultIgniteThinClientConfiguration.PREFIX + "." + "enabled", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
@Requires(property = DefaultIgniteConfiguration.PREFIX + "." + "enabled", value = StringUtils.FALSE, defaultValue = StringUtils.FALSE)
public class DefaultIgniteThinClientConfiguration implements Toggleable {
    public static final String PREFIX = "ignite-thin-client";
    private boolean enabled;

    @ConfigurationBuilder(excludes = {"transactionConfiguration", "binaryConfiguration", "sslContextFactory"})
    private final ClientConfiguration configuration = new ClientConfiguration();

    @ConfigurationBuilder(value = "transactionConfiguration")
    private final ClientTransactionConfiguration transaction = configuration.getTransactionConfiguration();

    /**
     * The default Ignite Thin configuration.
     */
    public DefaultIgniteThinClientConfiguration() {
    }

    /**
     * Sets whether the DefaultIgniteThinClientConfiguration is enabled.
     *
     * @param enabled True if it is.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The Ignite transaction.
     *
     * @return The ClientTransactionConfiguration
     */
    public final ClientTransactionConfiguration getTransaction() {
        return transaction;
    }

    /**
     * The Ignite ClientConfiguration.
     *
     * @return The ClientConfiguration
     */
    public final ClientConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
