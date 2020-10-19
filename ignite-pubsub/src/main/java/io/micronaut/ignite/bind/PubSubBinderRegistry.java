package io.micronaut.ignite.bind;

import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.ArgumentBinderRegistry;
import io.micronaut.core.type.Argument;

import java.util.Optional;

public class PubSubBinderRegistry implements ArgumentBinderRegistry<PubSubConsumerState> {
    @Override
    public <T> Optional<ArgumentBinder<T, PubSubConsumerState>> findArgumentBinder(Argument<T> argument, PubSubConsumerState source) {
        return Optional.empty();
    }
}
