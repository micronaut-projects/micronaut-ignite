import io.micronaut.context.ApplicationContext
import io.micronaut.ignite.configuration.IgniteClientConfiguration
import io.micronaut.inject.qualifiers.Qualifiers
import org.apache.ignite.spi.collision.fifoqueue.FifoQueueCollisionSpi
import org.apache.ignite.spi.collision.jobstealing.JobStealingCollisionSpi
import org.apache.ignite.spi.collision.priorityqueue.PriorityQueueCollisionSpi
import spock.lang.Specification

class IgniteCacheSpec extends Specification {

    void "test ignite cache instance"() {
        given:
        ApplicationContext ctx = ApplicationContext.run(ApplicationContext, [
            "ignite.enabled"                        : true,
            "ignite.clients.test.enabled"           : true,
            "ignite.clients.test.collision-spi.fifo-queue.enabled": true,
            "ignite.clients.one.enabled"            : true,
            "ignite.clients.one.collision-spi.priority-queue.enabled" : true,
        ])
        when:
        IgniteClientConfiguration test = ctx.getBean(IgniteClientConfiguration.class, Qualifiers.byName("test"))
        IgniteClientConfiguration one = ctx.getBean(IgniteClientConfiguration.class, Qualifiers.byName("one"))

        then:
        test.getConfiguration().getCollisionSpi() instanceof FifoQueueCollisionSpi
        one.getConfiguration().getCollisionSpi() instanceof PriorityQueueCollisionSpi

    }
}
