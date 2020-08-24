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
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.ignite.annotation.IgnitePrimary;
import java.util.Collection;
import org.apache.ignite.configuration.AtomicConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import static io.micronaut.ignite.configuration.IgniteIpFinder.*;


/**
 * Ignite cache configuration.
 */
@IgnitePrimary
@ConfigurationProperties(value = DefaultIgniteConfiguration.PREFIX, excludes = {"cacheConfiguration",
    "fileSystemConfiguration", "hadoopConfiguration"})
@Requires(property = DefaultIgniteThinClientConfiguration.PREFIX + "." + "enabled", value = StringUtils.FALSE, defaultValue = StringUtils.FALSE)
@Requires(property = DefaultIgniteConfiguration.PREFIX + "." + "enabled", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class DefaultIgniteConfiguration extends IgniteConfiguration implements Toggleable {
    public static final String PREFIX = "ignite";

    private boolean isEnabled;

    private IgniteIpFinder ipFinderType = STATIC;

    @ConfigurationBuilder(value = "communication-spi")
    final TcpCommunicationSpi communicationSpi = new TcpCommunicationSpi();

    @ConfigurationBuilder(value = "discovery-spi")
    final TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();

    @ConfigurationBuilder(value = "atomic-configuration")
    final AtomicConfiguration atomicConfiguration = new AtomicConfiguration();

    @ConfigurationBuilder(value = "discovery-spi.static-ip-finder")
    final TcpDiscoveryVmIpFinder staticIpFinder = new TcpDiscoveryVmIpFinder();

    @ConfigurationBuilder(value = "discovery-spi.kubernetes-ip-finder")
    final TcpDiscoveryKubernetesIpFinder kubernetesIpFinder = new TcpDiscoveryKubernetesIpFinder();

    /**
     * Default Ignite configuration.
     */
    DefaultIgniteConfiguration() {
        super.setCommunicationSpi(communicationSpi);
        super.setAtomicConfiguration(atomicConfiguration);

        switch (ipFinderType) {
            case STATIC:
                discoverySpi.setIpFinder(staticIpFinder);
                break;
            case KUBERNETES:
                discoverySpi.setIpFinder(kubernetesIpFinder);
                break;
            default:
                throw new RuntimeException("Unexpected IP finder type:" + ipFinderType);
        }

        super.setDiscoverySpi(discoverySpi);
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

    /**
     * Enables one of the IP finder that is to be used by the Ignite instance.
     *
     * @param ipFinderType one of the presently supported IP finders.
     */
    @ConfigurationBuilder(value = "discovery-spi.ip-finder-type")
    public void setIpFinderType(IgniteIpFinder ipFinderType) {
        this.ipFinderType = ipFinderType;
    }

    /**
     * Returns an IP finder used by the Ignite instance.
     *
     * @return
     */
    public IgniteIpFinder getIpFinderType() {
        return ipFinderType;
    }

    @EachProperty(value = "discovery-spi.static-ip-finder.addresses")
    public void setStaticIpFinderAddresses(Collection<String> addresses) {
        staticIpFinder.setAddresses(addresses);
    }
}
