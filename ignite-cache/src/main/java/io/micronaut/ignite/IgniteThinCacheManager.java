package io.micronaut.ignite;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.cache.DynamicCacheManager;
import io.micronaut.cache.SyncCache;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.scheduling.TaskExecutors;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

@Singleton
@Requires(beans = {IgniteClient.class})
public class IgniteThinCacheManager  implements DynamicCacheManager<ClientCache> {
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
        return new IgniteThinSyncCache(service, executorService, client.cache(name));
    }
}
