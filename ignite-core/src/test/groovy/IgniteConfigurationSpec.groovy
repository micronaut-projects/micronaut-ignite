import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.inject.qualifiers.Qualifiers
import org.apache.ignite.Ignite
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Singleton

@Testcontainers
@Retry
class IgniteConfigurationSpec extends Specification {
    final static String IGNITE_VERSION = System.getProperty("igniteVersion")

    @Shared
    GenericContainer ignite = new GenericContainer("apacheignite/ignite:${IGNITE_VERSION}")
        .withExposedPorts(10800)

    def "test ignite configuration"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            "ignite.enabled"    : true
        ])
        when:
        Ignite inst = ctx.getBean(Ignite.class,Qualifiers.byName("one"))

        then:
        inst != null
    }

}
