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
import io.micronaut.ignite.annotation.IgniteRef;
import io.micronaut.ignite.configuration.IgniteCacheConfiguration;
import io.micronaut.ignite.configuration.IgniteThinCacheConfiguration;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.context.scope.ThreadLocal;
import io.micronaut.runtime.http.scope.RequestScope;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;

import java.util.Optional;

@Factory
public class IgniteCacheFactory {

    public static final String REF_NAME = "value";
    public static final String REF_CLIENT = "client";

    private final BeanContext beanContext;

    /**
     * @param beanContext The bean context
     */
    public IgniteCacheFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    /**
     * get instance of {@link IgniteCache} defined by {@link IgniteRef}.
     *
     * @param annotationValue annotation value for ignite cache
     * @return An instance of the {@link ClientCache} from {@link Ignite}.
     */
    public IgniteCache getIgniteCache(AnnotationValue<IgniteRef> annotationValue) {
        String cacheName = annotationValue.stringValue(REF_CLIENT).orElse("default");
        Ignite ignite = beanContext.findBean(Ignite.class, Qualifiers.byName(cacheName))
            .orElseThrow(() -> new IllegalStateException("Failed to find bean" + cacheName));
        String name = annotationValue.stringValue(REF_NAME).orElseThrow(() -> new IllegalStateException("Missing value for cache"));
        return ignite.cache(name);
    }

    /**
     * Get {@link IgniteCache} from parameter annotated with {@link IgniteRef}.
     *
     * @param injectionPoint injection point for {@link IgniteCache}.
     * @return An instance of the {@link ClientCache} from {@link Ignite}.
     */
    @Prototype
    public IgniteCache getIgniteCache(InjectionPoint<IgniteCache> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        AnnotationValue<IgniteRef> igniteCache = metadata.findAnnotation(IgniteRef.class)
            .orElseThrow(() -> new IllegalStateException("Requires @IgniteCache"));
        return getIgniteCache(igniteCache);
    }

    /**
     * Get {@link ClientCache} from parameter annotated with {@link IgniteRef}.
     *
     * @param annotationValue annotation value
     * @return An instance of the {@link ClientCache} from {@link IgniteClient}.
     */
    public ClientCache getIgniteClientCache(AnnotationValue<IgniteRef> annotationValue) {
        String cacheName = annotationValue.stringValue(REF_CLIENT).orElse("default");
        IgniteClient client = beanContext.findBean(IgniteClient.class, Qualifiers.byName(cacheName))
            .orElseThrow(() -> new IllegalStateException("Failed to find bean" + cacheName));
        String name = annotationValue.stringValue(REF_NAME).orElseThrow(() -> new IllegalStateException("Missing value for cache"));
        return client.cache(name);
    }

    /**
     * Get {@link ClientCache} from parameter annotated with {@link IgniteRef}.
     *
     * @param injectionPoint injection point for {@link ClientCache}.
     * @return An instance of the {@link ClientCache} from {@link IgniteClient}.
     */
    @Prototype
    public ClientCache getIgniteClientCache(InjectionPoint<ClientCache> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        AnnotationValue<IgniteRef> igniteCache = metadata.findAnnotation(IgniteRef.class)
            .orElseThrow(() -> new IllegalStateException("Requires @IgniteCache"));
        return getIgniteClientCache(igniteCache);
    }

    public IgniteDataStreamer getIgniteDataStream(AnnotationValue<IgniteRef> annotationValue) {
        String cacheName = annotationValue.stringValue(REF_CLIENT).orElse("default");
        Ignite ignite = beanContext.findBean(Ignite.class, Qualifiers.byName(cacheName))
            .orElseThrow(() -> new IllegalStateException("Failed to find bean" + cacheName));
        String name = annotationValue.stringValue(REF_NAME).orElseThrow(() -> new IllegalStateException("Missing value for cache"));
        return ignite.dataStreamer(name);

    }

    @RequestScope
    public IgniteDataStreamer getIgniteDataStream(InjectionPoint<IgniteDataStreamer> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        AnnotationValue<IgniteRef> igniteCache = metadata.findAnnotation(IgniteRef.class)
            .orElseThrow(() -> new IllegalStateException("Requires @IgniteCache"));
        return getIgniteDataStream(igniteCache);
    }
}
