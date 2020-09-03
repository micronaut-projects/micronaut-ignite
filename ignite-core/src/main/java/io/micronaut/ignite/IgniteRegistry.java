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
package io.micronaut.ignite;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.ignite.annotation.IgniteCacheRef;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;

@Factory
public class IgniteRegistry {

    private final BeanContext beanContext;

    /**
     * Default constructor.
     *
     * @param beanContext      The bean context
     */
    public IgniteRegistry(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    /**
     * Create {@link IgniteCache} from the given injection point.
     *
     * @param injectionPoint The injection point
     * @param <K>            the key
     * @param <V>            the value
     * @return ignite cache
     */
    @Prototype
    @Bean
    protected <K, V> IgniteCache<K, V> igniteCache(InjectionPoint<?> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        return resolveIgniteCache(metadata);
    }

    /**
     * Create {@link IgniteCache} from metadata.
     *
     * @param metadata annotation metadata
     * @param <K>      key
     * @param <V>      value
     * @return The cache
     */
    public <K, V> IgniteCache<K, V> resolveIgniteCache(AnnotationMetadata metadata) {
        AnnotationValue<IgniteCacheRef> igniteCache = metadata.findAnnotation(IgniteCacheRef.class)
            .orElseThrow(() -> new IllegalStateException("Requires @IgniteCache"));
        String instance = igniteCache.stringValue("client").orElse("default");
        String name = igniteCache.stringValue("value").orElseThrow(() -> new IllegalStateException("Missing value for cache"));
        Ignite ignite = beanContext.getBean(Ignite.class, Qualifiers.byName(instance));
        return ignite.getOrCreateCache(name);
    }
}
