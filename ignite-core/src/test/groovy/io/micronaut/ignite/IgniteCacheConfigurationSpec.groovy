package io.micronaut.ignite

import io.micronaut.context.ApplicationContext
import org.apache.ignite.cache.QueryEntity
import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
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
        cacheConfiguration.first().queryEntities.size() == 2
        QueryEntity accEntity = cacheConfiguration.first().queryEntities.find({ k -> k.tableName == "ACCOUNTS"})
        QueryEntity bookEntity = cacheConfiguration.first().queryEntities.find({ k -> k.tableName == "BOOKS"})
        accEntity != null
        bookEntity != null
        accEntity.keyType == "String"
        bookEntity.keyType == "String"

        cleanup:
        ctx.close()
    }

    def "test ignite query-entity default cache configuration"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            "ignite.enabled"                                                          : true,
            "ignite.cache-configurations.accounts.query-entities[0].table-name"       : "ACCOUNTS",
            "ignite.cache-configurations.accounts.query-entities[0].key-type"         : "String",
            "ignite.cache-configurations.accounts.query-entities[0].fields.ID"        : "java.lang.Long",
            "ignite.cache-configurations.accounts.query-entities[0].fields.amount"    : "java.lang.Double",
            "ignite.cache-configurations.accounts.query-entities[0].fields.updateDate": "java.util.Date"
        ])
        when:
        IgniteConfiguration configuration = ctx.getBean(IgniteConfiguration.class)
        CacheConfiguration[] caches = configuration.getCacheConfiguration();

        then:
        caches.size() == 1
        CacheConfiguration cache = caches.first()
        cache.queryEntities.size() == 1

        when:
        QueryEntity queryEntity = cache.queryEntities.first()

        then:
        queryEntity.fields.containsKey(key)
        queryEntity.fields.get(key) == value

        where:
        key      | value
        "id"     | "java.lang.Long"
        "amount" | "java.lang.Double"
        "updateDate" | "java.util.Date"
    }

}
