package io.micronaut.ignite


import io.micronaut.context.ApplicationContext
import io.micronaut.ignite.configuration.IgniteCacheConfiguration
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cache.CacheRebalanceMode
import org.apache.ignite.configuration.CacheConfiguration
import spock.lang.Specification

class IgniteCacheSpec extends Specification {

    void "test ignite cache instance"() {
        given:
        ApplicationContext ctx = ApplicationContext.run(ApplicationContext, [
            "ignite.enabled"                            : true,
            "ignite.clients.default.path"               : "classpath:example/standard.cfg",
            "ignite.caches.counter.client"              : "default",
            "ignite.caches.counter.group-name"          : "test",
            "ignite.caches.counter.atomicity-mode"      : "ATOMIC",
            "ignite.caches.counter.backups"             : 4,
            "ignite.caches.counter.default-lock-timeout": 5000,
            "ignite.caches.counter.rebalance-mode"      : "NONE"
        ])
        when:
        Collection<IgniteCacheConfiguration> cacheConfiguration = ctx.getBeansOfType(IgniteCacheConfiguration.class)

        then:
        cacheConfiguration != null
        cacheConfiguration.size() == 1
        cacheConfiguration.first().client == "default"
        CacheConfiguration ch = cacheConfiguration.first().getConfiguration()
        ch.name == "counter"
        ch.getRebalanceMode() == CacheRebalanceMode.NONE
        ch.getDefaultLockTimeout() == 5000
        ch.getBackups() == 4
        ch.getGroupName() == "test"
        ch.atomicityMode == CacheAtomicityMode.ATOMIC
    }
}
