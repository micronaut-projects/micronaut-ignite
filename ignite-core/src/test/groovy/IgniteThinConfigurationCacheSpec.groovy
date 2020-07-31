import io.micronaut.context.ApplicationContext
import io.micronaut.ignite.configuration.IgniteThinCacheConfiguration
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cache.CacheRebalanceMode
import org.apache.ignite.client.ClientCacheConfiguration
import spock.lang.Specification

class IgniteThinConfigurationCacheSpec extends Specification {
    void "test ignite thin cache configuration"() {
        given:
        ApplicationContext ctx = ApplicationContext.run(ApplicationContext, [
            "ignite.enabled"                                        : true,
            "ignite.thin-client-caches.counter.group-name"          : "test",
            "ignite.thin-client-caches.counter.atomicity-mode"      : "ATOMIC",
            "ignite.thin-client-caches.counter.backups"             : 4,
            "ignite.thin-client-caches.counter.default-lock-timeout": 5000,
            "ignite.thin-client-caches.counter.rebalance-mode"      : "NONE"
        ])
        when:
        Collection<IgniteThinCacheConfiguration> cacheConfiguration = ctx.getBeansOfType(IgniteThinCacheConfiguration.class)

        then:
        cacheConfiguration != null
        cacheConfiguration.size() == 1
        ClientCacheConfiguration ch = cacheConfiguration.first().getConfiguration()
        cacheConfiguration.first().getName() == "counter"
        ch.getRebalanceMode() == CacheRebalanceMode.NONE
        ch.getDefaultLockTimeout() == 5000
        ch.getBackups() == 4
        ch.getGroupName() == "test"
        ch.atomicityMode == CacheAtomicityMode.ATOMIC
    }
}
