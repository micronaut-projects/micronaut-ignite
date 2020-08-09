package io.micronaut.ignite.event;

import io.micronaut.context.Qualifier;
import io.micronaut.context.event.ApplicationEvent;
import org.apache.ignite.Ignite;

import javax.annotation.concurrent.Immutable;
import java.util.Optional;

@Immutable
public class IgniteStartEvent extends ApplicationEvent {
    private final Ignite instance;
    private final Optional<Qualifier> qualifier;

    /**
     * Constructs a prototypical Event.
     *
     * @param instance The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public IgniteStartEvent(Qualifier qualifier, Ignite instance) {
        super(instance);
        this.qualifier = Optional.ofNullable(qualifier);
        this.instance = instance;
    }

    public Ignite getInstance() {
        return instance;
    }

    public Optional<Qualifier> getQualifier() {
        return qualifier;
    }
}
