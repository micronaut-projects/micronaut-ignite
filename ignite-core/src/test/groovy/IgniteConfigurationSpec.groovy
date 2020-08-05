import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import org.apache.ignite.Ignite
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
@Retry
class IgniteConfigurationSpec extends Specification {
    final static String IGNITE_VERSION = System.getProperty("igniteVersion")

    @Shared
    GenericContainer ignite = new GenericContainer("apacheignite/ignite:${IGNITE_VERSION}")
        .withExposedPorts(10800)

    def "test ignite configuration"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            "ignite.enabled"    : true
        ])
        when:
        Ignite inst = ctx.getBean(Ignite.class,Qualifiers.byName("one"))

        then:
        inst != null
    }

}
