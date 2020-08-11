package io.micronaut.ignite

import io.micronaut.context.annotation.Property
import io.micronaut.ignite.entities.User
import io.micronaut.ignite.repositories.UserRepository
import io.micronaut.test.annotation.MicronautTest
import org.springframework.jdbc.object.SqlQuery
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.AutoCleanup
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification
import org.apache.ignite.cache.query.SqlQuery;

import javax.inject.Inject

//
//"ignite.enabled"             : true,
//"ignite.clients.default.path": "classpath:ignite_data.cfg",
//"ignite.datasources.default.cache": "mydb",
//"ignite.datasources.default.schema-generate": "CREATE_DROP"

@Testcontainers
@Retry
@MicronautTest(rollback = false)
@Property(name = "ignite.enabled", value = "true")
@Property(name = "ignite.clients.default.path", value = "classpath:ignite_data.cfg")
@Property(name = "ignite.datasource.default-cache", value = "mydb")
@Property(name = "ignite.datasource.schema-generate", value = "CREATE_DROP")
class IgniteUserRepositorySpec extends Specification{
    final static String IGNITE_VERSION = System.getProperty("igniteVersion")

    @Shared
    @AutoCleanup
    GenericContainer ignite = new GenericContainer("apacheignite/ignite:${IGNITE_VERSION}")
        .withExposedPorts(47500, 47100)

    @Inject
    @Shared
    UserRepository userRepository;

    def setupSpec() {


        userRepository.deleteAll()
        userRepository.saveAll([
            new User(name: "Jeff", id: 0),
            new User(name: "James", id: 1)
        ])
    }

    void "test save one"() {
        when:"one is saved"
        def user = new User(name: "Joe", id: 100)
        userRepository.save(user)

        then:"the instance is persisted"
        Optional<User> u = userRepository.findById(user.id)
        u.isPresent()
        userRepository.count() == 3
        u.get().name == "Joe"
    }

}
