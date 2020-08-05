package io.micronaut.ignite;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.cache.DynamicCacheManager;
import io.micronaut.cache.SyncCache;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.scheduling.TaskExecutors;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

@Singleton
public class IgniteCacheManager implements DynamicCacheManager<IgniteCache> {
    private final Ignite ignite;
    private final ConversionService<?> service;
    private final ExecutorService executorService;

    public IgniteCacheManager(@Primary Ignite ignite,
                              ConversionService<?> service,
                              @Named(TaskExecutors.IO) ExecutorService executorService) {
        this.ignite = ignite;
        this.service = service;
        this.executorService = executorService;
    }

    @NonNull
    @Override
    public SyncCache<IgniteCache> getCache(String name) {
        return new IgniteSyncCache(service, ignite.getOrCreateCache(name), executorService);
    }
}
