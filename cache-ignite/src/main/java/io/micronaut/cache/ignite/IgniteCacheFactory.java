package io.micronaut.cache.ignite;

import io.micronaut.cache.ignite.configuration.IgniteCacheConfiguration;
import io.micronaut.cache.ignite.configuration.IgniteThinCacheConfiguration;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.TaskExecutors;
import org.apache.ignite.Ignite;
import org.apache.ignite.client.IgniteClient;

import javax.inject.Named;
import java.util.concurrent.ExecutorService;


/**
 * Factory class that creates a {@link org.apache.ignite.client.IgniteClient}, an {@link IgniteSyncCache}.
 *
 * @author Michael Pollind
 */
@Factory
public class IgniteCacheFactory{

    private final BeanContext beanContext;

    public IgniteCacheFactory(BeanContext beanContext){
        this.beanContext = beanContext;
    }

    /**
     * @param configuration   the configuration
     * @param service         the conversion service
     * @param executorService the executor
     * @return the sync cache
     * @throws Exception when client can't be found for cache
     */
    @EachBean(IgniteCacheConfiguration.class)
    public IgniteSyncCache syncCache(
        IgniteCacheConfiguration configuration,
        ConversionService<?> service,
        @Named(TaskExecutors.IO) ExecutorService executorService) {
        Ignite ignite = beanContext.getBean(Ignite.class, Qualifiers.byName(configuration.getClient()));
        return new IgniteSyncCache(service, ignite.getOrCreateCache(configuration.build()), executorService);
    }

    @EachBean(IgniteCacheConfiguration.class)
    public IgniteThinSyncCache syncCacheThin(IgniteThinCacheConfiguration configuration,
                                             ConversionService<?> service,
                                             @Named(TaskExecutors.IO) ExecutorService executorService) {
        IgniteClient ignite = beanContext.getBean(IgniteClient.class, Qualifiers.byName(configuration.getClient()));
        return new IgniteThinSyncCache(service, executorService, ignite.getOrCreateCache(configuration.getConfiguration()));
    }

}
