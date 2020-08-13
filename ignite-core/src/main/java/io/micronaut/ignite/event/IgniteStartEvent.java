/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    /**
     *
     * @return
     */
    public Ignite getInstance() {
        return instance;
    }

    /**
     *
     * @return
     */
    public Optional<Qualifier> getQualifier() {
        return qualifier;
    }
}
