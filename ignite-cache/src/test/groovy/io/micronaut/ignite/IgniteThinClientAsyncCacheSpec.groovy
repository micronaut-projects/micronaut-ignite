package io.micronaut.ignite

import io.micronaut.cache.tck.AbstractAsyncCacheSpec
import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.IgnoreIf
import spock.lang.Retry
import spock.lang.Shared

@Testcontainers
@Retry
@IgnoreIf({ System.getenv('GITHUB_WORKFLOW') })
class IgniteThinClientAsyncCacheSpec extends AbstractAsyncCacheSpec {

    final static String IGNITE_VERSION = System.getProperty("igniteVersion")

    @Shared
    GenericContainer ignite = new GenericContainer("apacheignite/ignite:${IGNITE_VERSION}")
        .withExposedPorts(47500, 47100)

    @Override
    ApplicationContext createApplicationContext() {
        return ApplicationContext.run([
            "ignite.enabled"                       : true,
            "ignite.thin-clients.default.addresses": ["127.0.0.1:${ignite.getMappedPort(10800)}"],
            "ignite.caches.counter.cache-type"     : "Thin",
            "ignite.caches.counter2.cache-type"    : "Thin",
            "ignite.caches.test.cache-type"        : "Thin"
        ])
    }
}
