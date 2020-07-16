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
package io.micronaut.ignite.configuration;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.naming.Named;

import javax.annotation.Nonnull;

/**
 *  Configuration class for an Ignite client.
 *
 *  @author Michael Pollind
 */
@EachProperty(value = IgniteClientConfiguration.PREFIX, primary = "default")
public class IgniteClientConfiguration implements Named {
    public static final String PREFIX = IgniteConfiguration.PREFIX + "." + "clients";

    private final String name;
    private String path;

    /**
     * @param name Name or key of the client.
     */
    public IgniteClientConfiguration(@Parameter String name) {
        this.name = name;
    }

    /**
     * path to load in bean configuration.
     *
     * @param path bean config to load.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * path to load in bean configuration.
     *
     * @return bean config to load.
     */
    public String getPath() {
        return path;
    }

    /**
     * @return name or key for client
     */
    @Nonnull
    @Override
    public String getName() {
        return name;
    }
}
