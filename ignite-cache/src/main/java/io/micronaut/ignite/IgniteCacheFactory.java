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

import io.micronaut.cache.AsyncCache;
import io.micronaut.cache.SyncCache;
import io.micronaut.context.BeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.ignite.annotation.IgniteCacheRef;
import io.micronaut.ignite.configuration.CacheConfiguration;
import io.micronaut.ignite.configuration.IgniteThinCacheConfiguration;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.TaskExecutors;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.client.ClientCache;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;


/**
 * Factory class that creates a {@link SyncCache}, an {@link AsyncCache}.
 *
 * @author Michael Pollind
 */
@Factory
public class IgniteCacheFactory {

    private final BeanContext beanContext;
    private final IgniteCacheBuilderFactory cacheFactory;
    private Map<Qualifier<?>, CacheConfiguration> configurationMap = new HashMap<>();

    public IgniteCacheFactory(BeanContext beanContext, IgniteCacheBuilderFactory cacheFactory, Collection<CacheConfiguration> cacheConfigurations) {
        this.beanContext = beanContext;
        this.cacheFactory = cacheFactory;
        for (CacheConfiguration configuration : cacheConfigurations) {
            configurationMap.put(Qualifiers.byName(configuration.getName()), configuration);
        }
    }


    /**
     * @param cacheConfiguration cache configuration
     * @param service            the conversion service
     * @param executorService    the executor
     * @return sync cache
     */
    @EachBean(CacheConfiguration.class)
    public SyncCache syncCache(CacheConfiguration cacheConfiguration,
                               ConversionService<?> service,
                               @Named(TaskExecutors.IO) ExecutorService executorService) {

        AnnotationValue<IgniteCacheRef> refAnnotationValue = AnnotationValue.builder(IgniteCacheRef.class)
            .member(IgniteCacheBuilderFactory.CACHE_NAME, cacheConfiguration.getName())
            .member(IgniteCacheBuilderFactory.CACHE_CLIENT, cacheConfiguration.getClient())
            .member(IgniteCacheBuilderFactory.CACHE_CONFIGURATION, cacheConfiguration.getConfiguration().orElse(null))
            .build();
        switch (cacheConfiguration.getCacheType()) {
            case Thin:
                ClientCache clientCache = cacheFactory.getIgniteClientCache(refAnnotationValue);
                return new IgniteThinSyncCache(service, executorService, clientCache);
            case Default:
            default:
                IgniteCache igniteCache = cacheFactory.getIgniteCache(refAnnotationValue);
                return new IgniteSyncCache(service, igniteCache, executorService);
        }
    }

    /**
     * @param cacheConfiguration cache configuration
     * @param service            the conversion service
     * @param executorService    the executor
     * @return sync cache
     */
    @EachBean(IgniteThinCacheConfiguration.class)
    public AsyncCache syncCacheThin(CacheConfiguration cacheConfiguration,
                                    ConversionService<?> service,
                                    @Named(TaskExecutors.IO) ExecutorService executorService) {

        AnnotationValue<IgniteCacheRef> refAnnotationValue = AnnotationValue.builder(IgniteCacheRef.class)
            .member(IgniteCacheBuilderFactory.CACHE_NAME, cacheConfiguration.getName())
            .member(IgniteCacheBuilderFactory.CACHE_CLIENT, cacheConfiguration.getClient())
            .member(IgniteCacheBuilderFactory.CACHE_CONFIGURATION, cacheConfiguration.getConfiguration().orElse(null))
            .build();
        switch (cacheConfiguration.getCacheType()) {
            case Thin:
                ClientCache clientCache = cacheFactory.getIgniteClientCache(refAnnotationValue);
                return new IgniteThinSyncCache(service, executorService, clientCache).async();
            case Default:
            default:
                IgniteCache igniteCache = cacheFactory.getIgniteCache(refAnnotationValue);
                return new IgniteAsyncCache(service, igniteCache, executorService);
        }
    }
}
