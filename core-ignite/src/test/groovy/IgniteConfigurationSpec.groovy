import io.micronaut.context.ApplicationContext
import org.apache.ignite.Ignite
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
@Retry
class IgniteConfigurationSpec extends Specification {

    @Shared
    GenericContainer ignite = new GenericContainer("apacheignite/ignite:2.8.0")
        .withExposedPorts(47500, 47100)

    def "test ignite instance"() {
        given:
        ApplicationContext ctx = ApplicationContext.run(ApplicationContext, [
            "ignite.enabled"            : true,
            "ignite.client.default.path": "classpath:example/test.cfg"
        ])
        when:
        Ignite ign = ctx.getBean(Ignite)

        then:
        ign != null

    }
}
