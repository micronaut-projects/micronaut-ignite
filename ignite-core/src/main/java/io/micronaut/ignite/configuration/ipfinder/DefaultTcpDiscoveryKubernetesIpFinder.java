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
package io.micronaut.ignite.configuration.ipfinder;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.ignite.annotation.IgnitePrimary;
import io.micronaut.ignite.configuration.DefaultIgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder;


/**
 * Micronaut configuration for {@link TcpDiscoveryKubernetesIpFinder}.
 */
@IgnitePrimary
@ConfigurationProperties(value = DefaultTcpDiscoveryKubernetesIpFinder.PREFIX)
@Requires(property = DefaultTcpDiscoveryKubernetesIpFinder.PREFIX + "." + "enabled", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class DefaultTcpDiscoveryKubernetesIpFinder extends TcpDiscoveryKubernetesIpFinder implements Toggleable {
    public static final String PREFIX = DefaultIgniteConfiguration.PREFIX_DISCOVERY + ".kubernetes-ip-finder";
    private boolean isEnabled;

    /**
     * Sets whether the DefaultTcpDiscoveryKubernetesIpFinder is enabled.
     *
     * @param enabled Ture if it is
     */
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
