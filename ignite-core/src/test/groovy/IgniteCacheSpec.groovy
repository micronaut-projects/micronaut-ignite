import io.micronaut.context.ApplicationContext
import io.micronaut.ignite.IgniteSampleCache
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.AutoCleanup
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
@Retry
class IgniteCacheSpec extends Specification {

    final static String IGNITE_VERSION = System.getProperty("igniteVersion")

    @Shared @AutoCleanup
    GenericContainer ignite = new GenericContainer("apacheignite/ignite:${IGNITE_VERSION}")
        .withExposedPorts(47500, 47100)

    void "test ignite inject cache"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            "ignite.enabled"             : true,
            "ignite.clients.default.path": "classpath:standard.cfg",
        ])
        when:
        IgniteSampleCache instance = ctx.getBean(IgniteSampleCache.class)
        then:
        instance.cache1 != null
        instance.cache2 != null
    }
}
