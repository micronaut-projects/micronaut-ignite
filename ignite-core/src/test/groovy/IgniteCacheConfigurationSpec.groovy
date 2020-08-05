import spock.lang.Specification

class IgniteCacheConfigurationSpec extends Specification {
//    void "test ignite cache configuration"() {
//        given:
//        ApplicationContext ctx = ApplicationContext.run(ApplicationContext, [
//            "ignite.enabled"                                   : true,
//            "ignite.client-caches.counter.group-name"          : "test",
//            "ignite.client-caches.counter.atomicity-mode"      : "ATOMIC",
//            "ignite.client-caches.counter.backups"             : 4,
//            "ignite.client-caches.counter.default-lock-timeout": 5000,
//            "ignite.client-caches.counter.rebalance-mode"      : "NONE"
//        ])
//        when:
//        Collection<IgniteCacheConfiguration> cacheConfiguration = ctx.getBeansOfType(IgniteCacheConfiguration.class)
//
//        then:
//        cacheConfiguration != null
//        cacheConfiguration.size() == 1
//        CacheConfiguration ch = cacheConfiguration.first().getConfiguration()
//        cacheConfiguration.first().getName() == "counter"
//        ch.getRebalanceMode() == CacheRebalanceMode.NONE
//        ch.getDefaultLockTimeout() == 5000
//        ch.getBackups() == 4
//        ch.getGroupName() == "test"
//        ch.atomicityMode == CacheAtomicityMode.ATOMIC
//        ch.getKeyType() == Object.class
//        ch.getValueType() == Object.class
//    }
}
