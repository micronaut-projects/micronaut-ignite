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
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.ignite.annotation.IgniteRef;
import io.micronaut.ignite.configuration.IgniteClientConfiguration;
import io.micronaut.ignite.event.IgniteStartEvent;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.http.scope.RequestScope;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.net.URL;
import java.util.Optional;

/**
 * Factory for the default implementation of {@link Ignite}.
 */
@Factory
@Requires(property = IgniteClientConfiguration.PREFIX + ".enabled", value = "true", defaultValue = "false")
public class IgniteFactory implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(IgniteFactory.class);

    private final ResourceResolver resourceResolver;
    private final BeanContext beanContext;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Default constructor.
     *
     * @param resourceResolver The resource resolver
     * @param beanContext      The bean context
     */
    public IgniteFactory(ResourceResolver resourceResolver, BeanContext beanContext, ApplicationEventPublisher eventPublisher) {
        this.resourceResolver = resourceResolver;
        this.beanContext = beanContext;
        this.eventPublisher = eventPublisher;
    }

    private <T> Qualifier<T> getContext(T item) {
        Optional<BeanRegistration<T>> registration = beanContext.findBeanRegistration(item);
        if (!registration.isPresent()) {
            return null;
        }
        BeanDefinition<T> definition = registration.get().getBeanDefinition();
        return definition.getDeclaredQualifier();
    }

    /**
     * Create {@link Ignite} instance from {@link IgniteClientConfiguration}.
     *
     * @param configuration ignite configuration
     * @return create ignite instance
     */
    @Singleton
    @EachBean(IgniteClientConfiguration.class)
    @Bean(preDestroy = "close")
    public Ignite ignite(IgniteClientConfiguration configuration) {
        try {
            Optional<URL> template = resourceResolver.getResource(configuration.getPath());
            if (!template.isPresent()) {
                throw new RuntimeException("failed to find configuration: " + configuration.getPath());
            }
            Ignite ignite = Ignition.start(template.get());
            eventPublisher.publishEvent(new IgniteStartEvent(getContext(configuration), ignite));
            return ignite;
        } catch (Exception e) {
            LOG.error("Failed to instantiate Ignite: " + e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Create {@link Ignite} instance from {@link IgniteConfiguration}.
     *
     * @param configuration ignite configuration
     * @return create ignite instance
     */
    @Singleton
    @EachBean(IgniteConfiguration.class)
    @Bean(preDestroy = "close")
    public Ignite ignite(IgniteConfiguration configuration) {
        try {
            Ignite ignite = Ignition.start(configuration);
            eventPublisher.publishEvent(new IgniteStartEvent(getContext(configuration), ignite));
            return ignite;
        } catch (Exception e) {
            LOG.error("Failed to instantiate Ignite Client: " + e.getMessage(), e);
            throw e;
        }
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
        AnnotationValue<IgniteRef> igniteCache = metadata.findAnnotation(IgniteRef.class)
            .orElseThrow(() -> new IllegalStateException("Requires @IgniteCache"));
        String client = igniteCache.stringValue("client").orElse("default");
        String name = igniteCache.stringValue("value").orElseThrow(() -> new IllegalStateException("Missing value for cache"));
        Ignite ignite = beanContext.getBean(Ignite.class, Qualifiers.byName(client));
        return ignite.cache(name);
    }

    /**
     * Create {@link IgniteDataStreamer} from the given injection point.
     *
     * @param injectionPoint The injection point
     * @param <K>            key
     * @param <V>            value
     * @return The data streamer
     */
    @RequestScope
    @Bean
    public <K, V> IgniteDataStreamer<K, V> igniteDataStreamer(InjectionPoint<?> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        return resolveDataStream(metadata);
    }

    /**
     * resolve {@link IgniteDataStreamer}.
     *
     * @param metadata annotation metadata
     * @param <K>      the key
     * @param <V>      the value
     * @return The data streamer
     */
    public <K, V> IgniteDataStreamer<K, V> resolveDataStream(AnnotationMetadata metadata) {
        AnnotationValue<IgniteRef> igniteCache = metadata.findAnnotation(IgniteRef.class)
            .orElseThrow(() -> new IllegalStateException("Requires @IgniteCache"));
        String client = igniteCache.stringValue("client").orElse("default");
        String name = igniteCache.stringValue("value").orElseThrow(() -> new IllegalStateException("Missing value for cache"));
        Ignite ignite = beanContext.getBean(Ignite.class, Qualifiers.byName(client));
        return ignite.dataStreamer(name);
    }

    @Override
    public void close() throws Exception {
        Ignition.stopAll(true);
    }
}
