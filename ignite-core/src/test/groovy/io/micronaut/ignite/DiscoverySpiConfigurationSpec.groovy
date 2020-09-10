package io.micronaut.ignite

import io.micronaut.context.ApplicationContext
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder
import spock.lang.Specification


class DiscoverySpiConfigurationSpec extends Specification {
    def "test static ip configuration"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            "ignite.enabled"                                    : true,
            "ignite.communication-spi.local-port"               : "localhost:1800",
            "ignite.discovery-spi.static-ip-finder.enabled"     : "true",
            "ignite.discovery-spi.static-ip-finder.addresses[0]": "127.0.0.1:47500",
            "ignite.discovery-spi.static-ip-finder.addresses[1]": "127.0.0.1:47501",
        ])
        when:
        IgniteConfiguration cfg = ctx.getBean(IgniteConfiguration.class)

        then:
        cfg != null

        TcpDiscoveryIpFinder ipFinder = ((TcpDiscoverySpi) cfg.discoverySpi).ipFinder;
        ipFinder instanceof TcpDiscoveryVmIpFinder
        ipFinder.registeredAddresses.contains(new InetSocketAddress("127.0.0.1", 47500))
        ipFinder.registeredAddresses.contains(new InetSocketAddress("127.0.0.1", 47501))
    }

    def "test Kubernetes ip configuration"() {
        ApplicationContext ctx = ApplicationContext.run([
            "ignite.enabled"                                     : true,
            "ignite.communication-spi.local-port"                : "localhost:1800",
            "ignite.discovery-spi.kubernetes-ip-finder.enabled"  : "true",
            "ignite.discovery-spi.kubernetes-ip-finder.namespace": "HelloWorld"
        ])
        when:
        IgniteConfiguration cfg = ctx.getBean(IgniteConfiguration.class)

        then:
        cfg != null

        TcpDiscoveryKubernetesIpFinder ipFinder = ((TcpDiscoverySpi) cfg.discoverySpi).ipFinder;
        ipFinder instanceof TcpDiscoveryKubernetesIpFinder
    }
}
