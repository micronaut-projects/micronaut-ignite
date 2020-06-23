package io.micronaut.ignite.configuration;

import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.naming.Named;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.collision.CollisionSpi;
import org.apache.ignite.spi.collision.fifoqueue.FifoQueueCollisionSpi;
import org.apache.ignite.spi.collision.jobstealing.JobStealingCollisionSpi;
import org.apache.ignite.spi.collision.priorityqueue.PriorityQueueCollisionSpi;
import org.apache.ignite.spi.deployment.local.LocalDeploymentSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.jdbc.TcpDiscoveryJdbcIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.sharedfs.TcpDiscoverySharedFsIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.spi.eventstorage.memory.MemoryEventStorageSpi;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@EachProperty(value = IgniteClientConfiguration.PREFIX)
public class IgniteClientConfiguration implements Named {
    public static final String PREFIX = IgniteConfig.PREFIX + "." + "clients";

    private final String name;

    @ConfigurationBuilder(excludes = {"Name"})
    private final IgniteConfiguration configuration = new IgniteConfiguration();

    @ConfigurationBuilder(value = "discovery-spi")
    private final TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();


    public IgniteClientConfiguration(@Parameter String name,
                                     @NotNull CollisionSpi collisionSpi) {
        this.name = name;
        configuration.setCollisionSpi(collisionSpi.getCollisionSpi());
        configuration.setIgniteInstanceName(name);
    }

    public TcpDiscoverySpi getDiscoverySpi() {
        return discoverySpi;
    }

    public IgniteConfiguration getConfiguration() {
        return configuration;
    }


//    @ConfigurationProperties("local-deployment-spi")
//    public static class LocalDeploymentSpiConfiguration extends LocalDeploymentSpi {}
//
//    @ConfigurationProperties("discovery-spi.event-storage.memory")
//    public static class MemoryEventStorageSpiConfiguration extends MemoryEventStorageSpi { }
//
//
//    @ConfigurationProperties("discovery-spi.jdbc")
//    public static class TcpDiscoveryJdbcIpFinderConfiguration extends TcpDiscoveryJdbcIpFinder{ }
//    @ConfigurationProperties("discovery-spi.multicast")
//    public static class TcpDiscoveryMulticastIpFinderConfiguration extends TcpDiscoveryMulticastIpFinder { }
//    @ConfigurationProperties("discovery-spi.shared-fs-finder")
//    public static class TcpDiscoverySharedFsIpFinderConfiguration extends TcpDiscoverySharedFsIpFinder { }
//    @ConfigurationProperties("discovery-spi.vm-ip-finder")
//    public static class TcpDiscoveryVmIpFinderConfiguration extends TcpDiscoveryVmIpFinder { }
//

    public static interface CollisionSpi<T extends org.apache.ignite.spi.collision.CollisionSpi> {
        T getCollisionSpi();
    }
    @ConfigurationProperties("fifo-queue")
    public static class FifoQueueCollisionSpiConfiguration implements CollisionSpi<FifoQueueCollisionSpi>{
        @ConfigurationBuilder
        public FifoQueueCollisionSpi configuration = new FifoQueueCollisionSpi();

        @Override
        public FifoQueueCollisionSpi getCollisionSpi() {
            return configuration;
        }
    }
    @ConfigurationProperties("job-stealing")
    public static class JobStealingCollisionSpiConfiguration implements CollisionSpi<JobStealingCollisionSpi> {
        @ConfigurationBuilder
        public JobStealingCollisionSpi configuration = new JobStealingCollisionSpi();
        @Override
        public JobStealingCollisionSpi getCollisionSpi() {
            return configuration;
        }
    }
    @ConfigurationProperties("priority-queue")
    public static class PriorityQueueCollisionSpiConfiguration implements CollisionSpi<PriorityQueueCollisionSpi>{
        @ConfigurationBuilder
        public PriorityQueueCollisionSpi configuration = new PriorityQueueCollisionSpi();
        @Override
        public PriorityQueueCollisionSpi getCollisionSpi() {
            return configuration;
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }
}
