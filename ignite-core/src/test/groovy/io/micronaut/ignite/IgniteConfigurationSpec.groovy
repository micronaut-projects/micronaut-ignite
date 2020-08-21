package io.micronaut.ignite

import io.micronaut.context.ApplicationContext
import io.micronaut.ignite.configuration.DefaultCacheConfiguration
import io.micronaut.ignite.configuration.DefaultIgniteConfiguration
import io.micronaut.inject.qualifiers.Qualifiers
import org.apache.ignite.Ignite
import org.apache.ignite.cache.QueryEntity
import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.spi.communication.CommunicationSpi
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi
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

    def "test ignite client instance is created"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            "ignite.enabled"    : true,
            "ignite.communication-spi.local-port": "${ignite.getMappedPort(10800)}",
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
            "ignite.cache-configurations.accounts.query-entities[1].table-name": "BOOKS",
            "ignite.cache-configurations.accounts.query-entities[1].key-type": "String"
        ])
        when:
        IgniteConfiguration configuration = ctx.getBean(IgniteConfiguration.class)
        TcpCommunicationSpi communicationSpi = (TcpCommunicationSpi)configuration.getCommunicationSpi();
        CacheConfiguration[] cacheConfiguration = configuration.getCacheConfiguration();


        then:
        configuration != null
        communicationSpi != null
        communicationSpi.getLocalPort() == 5555
        cacheConfiguration.size() == 1
        cacheConfiguration.first().name == "accounts"
        QueryEntity accEntity = cacheConfiguration.first().queryEntities.find({ k -> k.tableName == "ACCOUNTS"})
        QueryEntity bookEntity = cacheConfiguration.first().queryEntities.find({ k -> k.tableName == "BOOKS"})
        accEntity != null
        bookEntity != null
        accEntity.keyType == "String"
        bookEntity.keyType == "String"

        cleanup:
        ctx.close()
    }

}
