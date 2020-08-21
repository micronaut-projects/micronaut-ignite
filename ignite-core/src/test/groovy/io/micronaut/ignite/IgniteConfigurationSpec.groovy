package io.micronaut.ignite

import io.micronaut.context.ApplicationContext
import io.micronaut.ignite.configuration.DefaultCacheConfiguration
import io.micronaut.ignite.configuration.DefaultIgniteConfiguration
import io.micronaut.inject.qualifiers.Qualifiers
import org.apache.ignite.Ignite
import org.apache.ignite.configuration.CacheConfiguration
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
            "ignite.communication-spi.local-port": "5555",
            "ignite.cache-configurations.accounts.name": "accounts",
            "ignite.cache-configurations.accounts.query-entities[0].table-name": "ACCOUNTS",
            "ignite.cache-configurations.accounts.query-entities[0].key-type": "String",
            "ignite.cache-configurations.accounts.query-entities[1].table-name": "Books",
            "ignite.cache-configurations.accounts.query-entities[1].key-type": "String"
        ])
        when:
        DefaultIgniteConfiguration configuration = ctx.getBean(DefaultIgniteConfiguration.class)
        Collection<DefaultCacheConfiguration> cacheConfiguration = ctx.getBeansOfType(DefaultCacheConfiguration.class)
        CommunicationSpi communicationSpi = configuration.getCommunicationSpi();


        then:
        configuration != null
        cacheConfiguration != null;
        communicationSpi != null
        communicationSpi.getLocalPort() == 5555
        cacheConfiguration.size() == 1
        cacheConfiguration.first().name == "accounts"

        cleanup:
        ctx.close()
    }

}
