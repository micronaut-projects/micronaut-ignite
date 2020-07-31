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
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.ignite.annotation.IgniteCacheRef;
import io.micronaut.ignite.configuration.IgniteCacheConfiguration;
import io.micronaut.ignite.configuration.IgniteThinCacheConfiguration;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;

import java.util.Optional;

@Factory
public class IgniteCacheBuilderFactory {

    public static final String CACHE_NAME = "value";
    public static final String CACHE_CONFIGURATION = "configurationId";
    public static final String CACHE_CLIENT = "client";

    private final BeanContext beanContext;

    /**
     * @param beanContext The bean context
     */
    public IgniteCacheBuilderFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    /**
     * get instance of {@link IgniteCache} defined by {@link IgniteCacheRef}.
     *
     * @param annotationValue annotation value for ignite cache
     * @return An instance of the {@link ClientCache} from {@link Ignite}.
     */
    public IgniteCache getIgniteCache(AnnotationValue<IgniteCacheRef> annotationValue) {
        Optional<String> configurationId = annotationValue.stringValue(CACHE_CONFIGURATION);
        Ignite ignite = beanContext.getBean(Ignite.class, Qualifiers.byName(annotationValue.stringValue(CACHE_CLIENT).orElse("default")));
        String name = annotationValue.stringValue(CACHE_NAME).orElseThrow(() -> new IllegalStateException("Missing value for cache"));
        if (configurationId.isPresent() && !configurationId.get().isEmpty()) {
            IgniteCacheConfiguration cacheConfiguration = beanContext.getBean(IgniteCacheConfiguration.class, Qualifiers.byName(configurationId.get()));
            return ignite.getOrCreateCache(cacheConfiguration.getConfiguration(name));
        }
        return ignite.getOrCreateCache(name);
    }

    /**
     * Get {@link IgniteCache} from parameter annotated with {@link IgniteCacheRef}.
     *
     * @param injectionPoint injection point for {@link IgniteCache}.
     * @return An instance of the {@link ClientCache} from {@link Ignite}.
     */
    @Prototype
    public IgniteCache getIgniteCache(InjectionPoint<IgniteCache> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        AnnotationValue<IgniteCacheRef> igniteCache = metadata.findAnnotation(IgniteCacheRef.class)
            .orElseThrow(() -> new IllegalStateException("Requires @IgniteCache"));
        return getIgniteCache(igniteCache);
    }

    /**
     * Get {@link ClientCache} from parameter annotated with {@link IgniteCacheRef}.
     *
     * @param annotationValue annotation value
     * @return An instance of the {@link ClientCache} from {@link IgniteClient}.
     */
    public ClientCache getIgniteClientCache(AnnotationValue<IgniteCacheRef> annotationValue) {
        Optional<String> configurationId = annotationValue.stringValue(CACHE_CONFIGURATION);
        IgniteClient client = beanContext.getBean(IgniteClient.class, Qualifiers.byName(annotationValue.stringValue(CACHE_CLIENT).orElse("default")));
        String name = annotationValue.stringValue(CACHE_NAME).orElseThrow(() -> new IllegalStateException("Missing value for cache"));
        if (configurationId.isPresent() && !configurationId.get().isEmpty()) {
            IgniteThinCacheConfiguration thinCacheConfiguration = beanContext.getBean(IgniteThinCacheConfiguration.class, Qualifiers.byName(configurationId.get()));
            return client.getOrCreateCache(thinCacheConfiguration.getConfiguration(name));
        }
        return client.getOrCreateCache(name);
    }

    /**
     * Get {@link ClientCache} from parameter annotated with {@link IgniteCacheRef}.
     *
     * @param injectionPoint injection point for {@link ClientCache}.
     * @return An instance of the {@link ClientCache} from {@link IgniteClient}.
     */
    @Prototype
    public ClientCache getIgniteClientCache(InjectionPoint<ClientCache> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        AnnotationValue<IgniteCacheRef> igniteCache = metadata.findAnnotation(IgniteCacheRef.class)
            .orElseThrow(() -> new IllegalStateException("Requires @IgniteCache"));
        return getIgniteClientCache(igniteCache);
    }
}
