package io.micronaut.ignite

import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.GenericContainer
import spock.lang.Shared
import spock.lang.Specification

class GridfyAdviceSpec extends Specification {
    final static String IGNITE_VERSION = System.getProperty("igniteVersion")

    @Shared
    GenericContainer ignite = new GenericContainer("apacheignite/ignite:${IGNITE_VERSION}")
        .withExposedPorts(47500, 47100)

    void "test default Private"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            "ignite.enabled"             : true,
            "ignite.clients.default.path": "classpath:deploy_private.cfg"])

        when:
        TestGridifyTarget target = ctx.getBean(TestGridifyTarget.class)

        then:
        target.gridifyDefault("4") == 4
        target.gridifyDefault("10") == 10
        target.gridifyDefault("15") == 15
        target.gridifyDefault("20") == 20
    }

}
