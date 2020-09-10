package io.micronaut.ignite;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collections;

@Factory
public class IgniteConfigurationFactory {
    @Singleton
    @Bean
    @Named("one")
    IgniteConfiguration defaultConfiguration() {
        IgniteConfiguration cfg = new IgniteConfiguration();

        // The node will be started as a client node.
        cfg.setClientMode(true);
        // Setting up an IP Finder to ensure the client can locate the servers.
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Collections.singletonList("localhost:47500..47509"));
        cfg.setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(ipFinder));
        return cfg;
    }
}
