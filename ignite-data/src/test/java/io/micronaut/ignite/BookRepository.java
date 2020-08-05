package io.micronaut.ignite;

import io.micronaut.data.tck.repositories.AuthorRepository;
import io.micronaut.ignite.annotation.IgniteRepository;

@IgniteRepository(value = "book")
public abstract class BookRepository extends io.micronaut.data.tck.repositories.BookRepository {
    public BookRepository(AuthorRepository authorRepository) {
        super(authorRepository);
    }
}
