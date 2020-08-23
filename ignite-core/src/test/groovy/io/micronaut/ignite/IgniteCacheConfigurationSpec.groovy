package io.micronaut.ignite

import io.micronaut.context.ApplicationContext
import org.apache.ignite.cache.QueryEntity
import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi
import spock.lang.Specification

class IgniteCacheConfigurationSpec extends Specification {

    def "test ignite default cache configuration"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            "ignite.enabled"    : true,
            "ignite.cache-configurations.accounts.query-entities[0].table-name": "ACCOUNTS",
            "ignite.cache-configurations.accounts.query-entities[0].key-type": "String",
            "ignite.cache-configurations.accounts.query-entities[1].table-name": "BOOKS",
            "ignite.cache-configurations.accounts.query-entities[1].key-type": "String"
        ])
        when:
        IgniteConfiguration configuration = ctx.getBean(IgniteConfiguration.class)
        CacheConfiguration[] cacheConfiguration = configuration.getCacheConfiguration();


        then:
        configuration != null
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
