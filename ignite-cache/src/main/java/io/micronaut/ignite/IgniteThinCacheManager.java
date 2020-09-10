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

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.cache.DynamicCacheManager;
import io.micronaut.cache.SyncCache;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.StringUtils;
import io.micronaut.ignite.configuration.DefaultIgniteThinClientConfiguration;
import io.micronaut.scheduling.TaskExecutors;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

@Singleton
@Requires(beans = IgniteClient.class)
@Requires(property = DefaultIgniteThinClientConfiguration.PREFIX + "." + "enabled", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class IgniteThinCacheManager implements DynamicCacheManager<ClientCache> {
    private final IgniteClient client;
    private final ConversionService<?> service;
    private final ExecutorService executorService;

    public IgniteThinCacheManager(@Primary IgniteClient client,
                                  ConversionService<?> service,
                                  @Named(TaskExecutors.IO) ExecutorService executorService) {
        this.client = client;
        this.service = service;
        this.executorService = executorService;
    }

    @NonNull
    @Override
    public SyncCache<ClientCache> getCache(String name) {
        return new IgniteThinSyncCache(service, executorService, client.getOrCreateCache(name));
    }
}
