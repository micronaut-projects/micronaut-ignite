import io.micronaut.context.ApplicationContext
import io.micronaut.ignite.configuration.IgniteClientConfiguration
import org.apache.ignite.spi.collision.jobstealing.JobStealingCollisionSpi
import spock.lang.Specification

class IgniteCacheSpec extends Specification {

    void "test ignite cache instance"() {
        given:
        ApplicationContext ctx = ApplicationContext.run(ApplicationContext, [
            "ignite.enabled"                                                : true,
            "ignite.client.default.force-return-values"          : true,
            "ignite.client.default.collision-spi.job-stealing.enabled"          : true,
        ])
        when:
        Collection<IgniteClientConfiguration> cacheConfiguration = ctx.getBeansOfType(IgniteClientConfiguration.class)

        then:
        cacheConfiguration != null
        cacheConfiguration.size() == 1
        cacheConfiguration.first().getConfiguration().collisionSpi instanceof JobStealingCollisionSpi
    }
}
