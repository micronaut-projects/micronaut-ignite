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
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.ignite.annotation.IgnitePrimary;
import org.apache.ignite.configuration.AtomicConfiguration;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Optional;

/**
 * Ignite cache configuration.
 */
@Named("default")
@Primary
@ConfigurationProperties(value = DefaultIgniteConfiguration.PREFIX, excludes = {"cacheConfiguration",
    "fileSystemConfiguration", "hadoopConfiguration", "discoveryStartupDelay", "loadBalancingSpi",
    "indexingSpi", "encryptionSpi", "metricExporterSpi", "systemViewExporterSpi", "discoverySpi", "atomicConfiguration",
    "cacheStoreSessionListenerFactories", "pluginProviders", "failureHandler", "clientConnectorConfiguration",
    "sqlConnectorConfiguration", "executorConfiguration", "platformConfiguration", "pluginConfigurations", "transactionConfiguration",
    "warmupClosure", "serviceConfiguration", "odbcConfiguration", "connectorConfiguration", "persistentStoreConfiguration",
    "memoryConfiguration", "binaryConfiguration", "dataStorageConfiguration", "failoverSpi", "checkpointSpi", "deploymentSpi", "collisionSpi",
    "communicationSpi", "eventStorageSpi", "sslContextFactory", "lifecycleBeans", "marshaller", "MBeanServer", "gridLogger", "segmentationResolvers",
    "addressResolver", "cacheKeyConfiguration", "communicationFailureResolver"})
@Requires(property = DefaultIgniteThinClientConfiguration.PREFIX + "." + "enabled", value = StringUtils.FALSE, defaultValue = StringUtils.FALSE)
@Requires(property = DefaultIgniteConfiguration.PREFIX + "." + "enabled", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class DefaultIgniteConfiguration extends IgniteConfiguration implements Toggleable {
    public static final String PREFIX = "ignite";
    public static final String PREFIX_DISCOVERY = "ignite.discovery-spi";

    @ConfigurationBuilder(value = "communication-spi", excludes = {"addressResolver", "listener"})
    final TcpCommunicationSpi communicationSpi = new TcpCommunicationSpi();

    @ConfigurationBuilder(value = "discovery-spi", excludes = {"authenticator", "dataExchange",
        "listener", "metricsProvider", "internalListener", "ipFinder", "addressResolver"})
    final TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();

    @ConfigurationBuilder(value = "atomic-configuration")
    final AtomicConfiguration atomicConfiguration = new AtomicConfiguration();

    private boolean enabled;

    /**
     * Default Ignite configuration.
     *
     * @param ipFinder            ip finder
     * @param cacheConfigurations list of cacheConfigurations
     */
    @Inject
    DefaultIgniteConfiguration(@IgnitePrimary Optional<TcpDiscoveryIpFinder> ipFinder,
                               @IgnitePrimary Collection<CacheConfiguration> cacheConfigurations) {
        super.setCommunicationSpi(communicationSpi);
        super.setAtomicConfiguration(atomicConfiguration);
        ipFinder.ifPresent(discoverySpi::setIpFinder);
        super.setCacheConfiguration(cacheConfigurations.toArray(new CacheConfiguration[0]));
        super.setDiscoverySpi(discoverySpi);
    }

    /**
     * Sets whether the DefaultIgniteConfiguration is enabled.
     *
     * @param enabled True if it is.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
