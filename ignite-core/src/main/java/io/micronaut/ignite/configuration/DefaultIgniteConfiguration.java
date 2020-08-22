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
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;

/**
 * Ignite cache configuration.
 */
@ConfigurationProperties(value = DefaultIgniteConfiguration.PREFIX, excludes = {"cacheConfiguration"})
@Requires(property = DefaultIgniteThinClientConfiguration.PREFIX + "." + "enabled", value = StringUtils.FALSE, defaultValue = StringUtils.FALSE)
@Requires(property = DefaultIgniteConfiguration.PREFIX + "." + "enabled", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class DefaultIgniteConfiguration extends IgniteConfiguration implements Toggleable {
    public static final String PREFIX = "ignite";

    private boolean isEnabled;

    @ConfigurationBuilder(value = "communicationSpi")
    final TcpCommunicationSpi communicationSpi = new TcpCommunicationSpi();

    /**
     * Default Ignite configuration.
     */
    DefaultIgniteConfiguration() {
        super.setCommunicationSpi(communicationSpi);
    }

    /**
     * Sets whether the DefaultIgniteConfiguration is enabled.
     *
     * @param enabled True if it is.
     */
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
