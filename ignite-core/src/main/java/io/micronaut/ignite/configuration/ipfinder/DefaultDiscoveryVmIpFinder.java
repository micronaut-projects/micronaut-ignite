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
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.ignite.annotation.IgnitePrimary;
import io.micronaut.ignite.configuration.DefaultIgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import java.util.Collection;

/**
 * Micronaut configuration for {@link TcpDiscoveryVmIpFinder}.
 */
@IgnitePrimary
@ConfigurationProperties(value = DefaultDiscoveryVmIpFinder.PREFIX, excludes = {"static-ip-finder-addresses"})
@Requires(property = DefaultDiscoveryVmIpFinder.PREFIX + "." + "enabled", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class DefaultDiscoveryVmIpFinder extends TcpDiscoveryVmIpFinder implements Toggleable {
    public static final String PREFIX = DefaultIgniteConfiguration.PREFIX_DISCOVERY + ".static-ip-finder";
    private boolean enabled;

    /**
     * vm ip finder is enabled.
     *
     * @param enabled is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EachProperty(value = "addresses")
    public void setStaticIpFinderAddresses(Collection<String> addresses) {
        this.setAddresses(addresses);
    }
}
