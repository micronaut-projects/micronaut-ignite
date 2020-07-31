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

import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.naming.Named;
import org.apache.ignite.configuration.CacheConfiguration;

/**
 * Ignite cache configuration.
 */
@EachProperty(value = IgniteCacheConfiguration.PREFIX, primary = "default")
public class IgniteCacheConfiguration<K,V> extends IgniteAbstractCacheConfiguration<K,V> {
    public static final String PREFIX = IgniteAbstractConfiguration.PREFIX + "." + "client-caches";

    private Class<K> keyType = (Class<K>) Object.class;
    private Class<V> ValueType = (Class<V>) Object.class;

    public void setKeyType(Class<K> keyType) {
        this.keyType = keyType;
    }

    public void setValueType(Class<V> valueType) {
        ValueType = valueType;
    }

    public Class<V> getValueType() {
        return ValueType;
    }

    public Class<K> getKeyType() {
        return keyType;
    }

    @ConfigurationBuilder(excludes = {"Name"})
    private final CacheConfiguration<K, V> configuration = new CacheConfiguration<K, V>();

    /**
     * @param name Name or key for client.
     */
    public IgniteCacheConfiguration(@Parameter String name) {
        super(name);
    }

    /**
     * Get Ignite Cache Configuration.
     *
     * @return CacheConfiguration.
     */
    public CacheConfiguration<K, V> getConfiguration() {
        return configuration;
    }

    /**
     * @param name get configuration from name
     * @return ignite cache configuration.
     */
    public CacheConfiguration<K, V> getConfiguration(String name) {
        return new CacheConfiguration<K, V>(configuration)
            .setName(name)
            .setTypes(keyType, ValueType);
    }
}
