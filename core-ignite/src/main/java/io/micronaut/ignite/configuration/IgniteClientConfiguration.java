package io.micronaut.ignite.configuration;

import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.naming.Named;
import io.micronaut.core.util.Toggleable;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.collision.CollisionSpi;
import org.apache.ignite.spi.collision.fifoqueue.FifoQueueCollisionSpi;
import org.apache.ignite.spi.collision.jobstealing.JobStealingCollisionSpi;
import org.apache.ignite.spi.collision.priorityqueue.PriorityQueueCollisionSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.jdbc.TcpDiscoveryJdbcIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.sharedfs.TcpDiscoverySharedFsIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.spi.loadbalancing.LoadBalancingSpi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Optional;

@EachProperty(value = IgniteClientConfiguration.PREFIX, primary = "default")
public class IgniteClientConfiguration implements Named {
    public static final String PREFIX = IgniteConfig.PREFIX + "." + "client";

    private final String name;

    @ConfigurationBuilder(excludes = {"Name"})
    private final IgniteConfiguration configuration = new IgniteConfiguration();

    @ConfigurationBuilder(value = "discovery-spi")
    private final TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();

    @Inject
    public IgniteClientConfiguration(@Parameter String name,
                                     Optional<CollisionSpi> colSpi,
                                     Optional<TcpDiscoveryIpFinder> discoveryIpFinder,
                                     Optional<LoadBalancingSpi[]> loadBalancingSpis) {
        this.name = name;
        colSpi.ifPresent(configuration::setCollisionSpi);
        configuration.setDiscoverySpi(discoverySpi);
        discoveryIpFinder.ifPresent(discoverySpi::setIpFinder);
        loadBalancingSpis.ifPresent(configuration::setLoadBalancingSpi);
        configuration.setIgniteInstanceName(name);
    }

    public IgniteConfiguration getConfiguration() {
        return configuration;
    }

    @ConfigurationProperties("discovery-spi.jdbc")
    public static class TcpDiscoveryJdbcIpFinderConfiguration extends TcpDiscoveryJdbcIpFinder implements Toggleable { }
    @ConfigurationProperties("discovery-spi.multicast")
    public static class TcpDiscoveryMulticastIpFinderConfiguration extends TcpDiscoveryMulticastIpFinder implements Toggleable { }
    @ConfigurationProperties("discovery-spi.filesystem")
    public static class TcpDiscoverySharedFsIpFinderConfiguration extends TcpDiscoverySharedFsIpFinder implements Toggleable { }
    @ConfigurationProperties("discovery-spi.vm")
    public static class TcpDiscoveryVmIpFinderConfiguration extends TcpDiscoveryVmIpFinder implements Toggleable { }

    @ConfigurationProperties("collision-spi.fifo")
    public static class FifoQueueCollisionSpiConfiguration extends FifoQueueCollisionSpi implements Toggleable {}
    @ConfigurationProperties("collision-spi.job-stealing")
    public static class JobStealingCollisionSpiConfiguration extends JobStealingCollisionSpi  implements Toggleable {}
    @ConfigurationProperties("collision-spi.priority-queue")
    public static class PriorityQueueCollisionSpiConfiguration extends PriorityQueueCollisionSpi  implements Toggleable {}

    @Nonnull
    @Override
    public String getName() {
        return name;
    }
}
