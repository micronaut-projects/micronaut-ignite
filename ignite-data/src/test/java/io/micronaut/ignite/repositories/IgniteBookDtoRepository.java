package io.micronaut.ignite.repositories;

import io.micronaut.data.tck.repositories.BookDtoRepository;
import io.micronaut.ignite.annotation.IgniteRepository;

@IgniteRepository(value = "default")
public interface IgniteBookDtoRepository extends BookDtoRepository {
}
