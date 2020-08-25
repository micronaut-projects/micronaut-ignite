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
class IgniteClientAsyncCacheSpec extends AbstractAsyncCacheSpec {
    final static String IGNITE_VERSION = System.getProperty("igniteVersion")

    @Shared
    GenericContainer ignite = new GenericContainer("apacheignite/ignite:${IGNITE_VERSION}")
        .withExposedPorts(47500, 47100)

    @Override
    ApplicationContext createApplicationContext() {
        return ApplicationContext.run([
            "ignite.enabled"                     : true,
            "ignite.cache.enabled"               : true,
            "ignite.client-mode"                 : true,
            "ignite.communication-spi.local-port": "${ignite.getMappedPort(10800)}",
        ])
    }
}
