import io.micronaut.context.ApplicationContext

class IgniteThinClientConfigurationSpec {
    def "test ignite cache instance"() {
        given:
        ApplicationContext ctx = ApplicationContext.run(ApplicationContext, [
            "ignite.enabled"                                      : true,
            "ignite.clients.default.force-return-values"          : true,
            "ignite.clients.default.client-mode"                  : true,
            "ignite.clients.default.discovery.multicast.addresses": ["localhost:47500..47509"],
            "ignite.caches.counter.client"                        : "default",
            "ignite.caches.counter.group-name"                    : "test",
            "ignite.caches.counter.atomicity-mode"                : "ATOMIC",
            "ignite.caches.counter.backups"                       : 4,
            "ignite.caches.counter.default-lock-timeout"          : 5000,
            "ignite.caches.counter.rebalance-mode"                : "NONE"
        ])
    }
}
