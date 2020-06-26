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
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.ignite.configuration.IgniteCacheConfiguration;
import io.micronaut.ignite.configuration.IgniteThinCacheConfiguration;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.TaskExecutors;
import org.apache.ignite.Ignite;
import org.apache.ignite.client.IgniteClient;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;


/**
 * Factory class that creates a {@link IgniteClient}, an {@link IgniteSyncCache}.
 *
 * @author Michael Pollind
 */
@Factory
public class IgniteCacheFactory {

    private final BeanContext beanContext;

    public IgniteCacheFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    /**
     * /**
     *
     * @param configuration   the configuration
     * @param service         the conversion service
     * @param executorService the executor
     * @return the sync cache
     */
    @EachBean(IgniteCacheConfiguration.class)
    public IgniteSyncCache syncCache(
        IgniteCacheConfiguration configuration,
        ConversionService<?> service,
        @Named(TaskExecutors.IO) ExecutorService executorService) {
        Ignite ignite = beanContext.getBean(Ignite.class, Qualifiers.byName(configuration.getClient()));
        return new IgniteSyncCache(service, ignite.getOrCreateCache(configuration.getConfiguration()), executorService);
    }

    @EachBean(IgniteThinCacheConfiguration.class)
    public IgniteThinSyncCache syncCacheThin(IgniteThinCacheConfiguration configuration,
                                             ConversionService<?> service,
                                             @Named(TaskExecutors.IO) ExecutorService executorService) {
        IgniteClient ignite = beanContext.getBean(IgniteClient.class, Qualifiers.byName(configuration.getClient()));
        return new IgniteThinSyncCache(service, executorService, ignite.getOrCreateCache(configuration.getConfiguration()));
    }
}
