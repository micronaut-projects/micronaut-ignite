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
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.scheduling.TaskExecutors;
import org.apache.ignite.client.ClientCache;

import javax.inject.Named;
import java.util.concurrent.ExecutorService;


/**
 * Factory class that creates a {@link SyncCache}, an {@link AsyncCache}.
 *
 * @author Michael Pollind
 */
@Factory
public class CacheFactory {
    @Prototype
    public IgniteSyncCache igniteSyncCache(InjectionPoint<?> injectionPoint,
                                           IgniteFactory igniteFactory,
                                           ConversionService<?> service,
                                           @Named(TaskExecutors.IO) ExecutorService executorService) {

        AnnotationMetadata annotationMetadata = injectionPoint.getAnnotationMetadata();
        return new IgniteSyncCache(service, igniteFactory.resolveIgniteCache(annotationMetadata), executorService);
    }

    @Prototype
    public IgniteAsyncCache igniteAsyncCache(InjectionPoint<?> injectionPoint,
                                             IgniteFactory igniteFactory,
                                             ConversionService<?> service,
                                             @Named(TaskExecutors.IO) ExecutorService executorService) {

        AnnotationMetadata annotationMetadata = injectionPoint.getAnnotationMetadata();
        return new IgniteAsyncCache(service, igniteFactory.resolveIgniteCache(annotationMetadata), executorService);
    }

    @Prototype
    public IgniteThinSyncCache igniteThinSyncCache(InjectionPoint<?> injectionPoint,
                                                   IgniteThinClientFactory igniteThinClientFactory,
                                                   ConversionService<?> service,
                                                   @Named(TaskExecutors.IO) ExecutorService executorService) {
        AnnotationMetadata annotationMetadata = injectionPoint.getAnnotationMetadata();
        return new IgniteThinSyncCache(service, executorService, igniteThinClientFactory.resolveClientCache(annotationMetadata));
    }

    @Prototype
    public AsyncCache<ClientCache> igniteThinAsyncCache(InjectionPoint<?> injectionPoint,
                                                        IgniteThinClientFactory igniteThinClientFactory,
                                                        ConversionService<?> service,
                                                        @Named(TaskExecutors.IO) ExecutorService executorService) {
        AnnotationMetadata annotationMetadata = injectionPoint.getAnnotationMetadata();
        return new IgniteThinSyncCache(service, executorService, igniteThinClientFactory.resolveClientCache(annotationMetadata)).async();
    }
}
