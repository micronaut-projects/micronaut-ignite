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

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.naming.Named;

import java.util.Optional;

@EachProperty(value = CacheConfiguration.PREFIX, primary = "default")
public class CacheConfiguration implements Named {
    public static final String PREFIX = IgniteAbstractConfiguration.PREFIX + "." + "caches";

    private String configuration;
    private String client = "default";
    private final String name;
    private CacheType cacheType = CacheType.Default;

    /**
     * The type of cache to use.
     *
     * @param cacheType the cache type
     */
    public void setCacheType(CacheType cacheType) {
        this.cacheType = cacheType;
    }

    /**
     * The type of cache.
     *
     * @return the cache type.
     */
    public CacheType getCacheType() {
        return cacheType;
    }

    /**
     * the cache configuration to reference: {@link IgniteThinCacheConfiguration}, {@link IgniteCacheConfiguration}.
     *
     * @param name the configuration to use
     */
    public CacheConfiguration(@Parameter String name) {
        this.name = name;
    }

    /**
     * The ignite client to use by name.
     *
     * @return the client name
     */
    public String getClient() {
        return client;
    }

    /**
     * The ignite client to use by name.
     *
     * @param client the client name
     */
    public void setClient(String client) {
        this.client = client;
    }

    /**
     * The configuration to use for loading client.
     *
     * @return the configuration name
     */
    public Optional<String> getConfiguration() {
        return Optional.ofNullable(configuration);
    }

    /**
     * The configuration to use for loading client.
     *
     * @param configuration the configuration name
     */
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    /**
     * The name.
     *
     * @return The name.
     */
    @NonNull
    @Override
    public String getName() {
        return name;
    }

    /**
     * Cache type.
     */
    public enum CacheType {
        Default,
        Thin
    }
}
