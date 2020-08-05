package io.micronaut.ignite

import io.micronaut.context.annotation.Property
import io.micronaut.data.tck.repositories.AuthorRepository
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import javax.inject.Inject

@MicronautTest(rollback = false, packages = ["io.micronaut.data.tck.entities"])
@Stepwise
@Property(name = "ignite.enabled", value = "true")
@Property(name = "ignite.clients.default.path", value = "classpath:ignite_projection_spec.cfg")
class ProjectionSpec extends Specification{
//    @Inject
//    @Shared
//    PersonCrudRepository crudRepository

    @Inject
    @Shared
    AuthorRepository authorRepository

    @Inject
    @Shared
    BookRepository bookRepository

//    @Inject
//    @Shared
//    OrderRepo orderRepository
//
//    @Inject
//    @Shared
//    PetRepository petRepository


    def setupSpec() {

    }
}
