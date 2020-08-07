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
package io.micronaut.ignite.config;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.naming.Named;
import io.micronaut.data.runtime.config.SchemaGenerate;
import io.micronaut.ignite.configuration.IgniteClientConfiguration;
import jdk.internal.joptsimple.internal.Strings;

import java.util.ArrayList;
import java.util.List;

@EachProperty(value = IgniteDataConfiguration.PREFIX, primary = "default")
public class IgniteDataConfiguration implements Named {
    public static final String PREFIX = IgniteClientConfiguration.PREFIX + ".datasources";

    private final String name;
    private SchemaGenerate schemaGenerate = SchemaGenerate.NONE;
    private boolean batchGenerate = false;
    private String client = "default";
    private List<String> packages = new ArrayList<>(3);
    private String cache;

    public IgniteDataConfiguration(@Parameter String name) {
        this.name = name;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getClient() {
        return client;
    }

    public void setCache(String cache) {
        this.cache = cache;
    }

    public String getCache() {
        return cache;
    }

    @NonNull
    @Override
    public String getName() {
        return this.name;
    }
}
