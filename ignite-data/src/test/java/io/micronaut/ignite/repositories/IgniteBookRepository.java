package io.micronaut.ignite.repositories;

import io.micronaut.data.tck.repositories.AuthorRepository;
import io.micronaut.data.tck.repositories.BookRepository;
import io.micronaut.ignite.annotation.IgniteRepository;

@IgniteRepository(value = "default")
public abstract class IgniteBookRepository extends BookRepository {
    public IgniteBookRepository(AuthorRepository authorRepository) {
        super(authorRepository);
    }
}
