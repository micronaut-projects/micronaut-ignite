package io.micronaut.ignite.repositories;

import io.micronaut.data.repository.CrudRepository;
import io.micronaut.ignite.annotation.IgniteRepository;
import io.micronaut.ignite.entities.User;

@IgniteRepository(value = "default", schema = "mydb")
public interface UserRepository extends CrudRepository<User, Long> {
}
