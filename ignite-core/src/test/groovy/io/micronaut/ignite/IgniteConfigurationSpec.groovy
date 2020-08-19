package io.micronaut.ignite

import io.micronaut.context.ApplicationContext
import io.micronaut.ignite.configuration.DefaultIgniteConfiguration
import io.micronaut.inject.qualifiers.Qualifiers
import org.apache.ignite.Ignite
import org.apache.ignite.spi.communication.CommunicationSpi
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.AutoCleanup
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
@Retry
class IgniteConfigurationSpec extends Specification {
    final static String IGNITE_VERSION = System.getProperty("igniteVersion")

    @Shared @AutoCleanup
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

        cleanup:
        ctx.close()
    }

    def "test ignite from xml config"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            "ignite.enabled"    : true,
            "ignite.clients.default.path": "classpath:standard.cfg",
        ])
        when:
        Ignite inst = ctx.getBean(Ignite.class)

        then:
        inst != null

        cleanup:
        ctx.close()
    }

    def "test ignite communication-spi"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            "ignite.enabled"    : true,
            "ignite.clients.default.path": "classpath:standard.cfg",
            "ignite.clients.default.communication-spi.local-port": "5555",
            "ignite.clients.default.cache-configurations[0].name": "accounts",
        ])
        when:
        DefaultIgniteConfiguration configuration = ctx.getBean(DefaultIgniteConfiguration.class)
        CommunicationSpi communicationSpi = configuration.getCommunicationSpi();
        List<DefaultIgniteConfiguration.DefaultCacheConfiguration<?, ?>> cacheConfigurationList =  configuration.getCacheConfigurations();



        then:
        configuration != null
        cacheConfigurationList != null;
        communicationSpi != null
        communicationSpi.getLocalPort() == 5555
        cacheConfigurationList.size() == 1
        cacheConfigurationList.first().name == "accounts"

        cleanup:
        ctx.close()
    }

}
