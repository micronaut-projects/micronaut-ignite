package io.micronaut.ignite;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.ignite.annotation.IgniteRef;
import io.micronaut.ignite.configuration.IgniteClientConfiguration;
import io.micronaut.ignite.configuration.IgniteThinClientConfiguration;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Factory
@Requires(property = IgniteClientConfiguration.PREFIX + ".enabled", value = "true", defaultValue = "false")
public class IgniteThinClientFactory {
    private static final Logger LOG = LoggerFactory.getLogger(IgniteThinClientFactory.class);

    private final BeanContext beanContext;

    public IgniteThinClientFactory(ResourceResolver resourceResolver, BeanContext beanContext) {
        this.beanContext = beanContext;
    }


    /**
     * Create a singleton {@link IgniteClient} client, based on an existing {@link IgniteThinClientConfiguration} bean.
     *
     * @param configuration the configuration read it as a bean
     * @return {@link IgniteClient}
     */
    @Singleton
    @EachBean(IgniteThinClientConfiguration.class)
    @Bean(preDestroy = "close")
    public IgniteClient createIgniteThinClient(IgniteThinClientConfiguration configuration) {
        try {
            return Ignition.startClient(configuration.getConfiguration());
        } catch (Exception e) {
            LOG.error("Failed to instantiate Ignite Client: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get {@link ClientCache} from parameter annotated with {@link IgniteRef}.
     *
     * @param injectionPoint injection point for {@link ClientCache}.
     * @return An instance of the {@link ClientCache} from {@link IgniteClient}.
     */
    @Prototype
    @Bean
    public ClientCache clientCache(InjectionPoint<?> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        return resolveClientCache(metadata);
    }

    /**
     * @param metadata annotation value
     * @return An instance of the {@link ClientCache} from {@link IgniteClient}.
     */
    protected ClientCache resolveClientCache(AnnotationMetadata metadata) {
        AnnotationValue<IgniteRef> igniteCache = metadata.findAnnotation(IgniteRef.class)
            .orElseThrow(() -> new IllegalStateException("Requires @IgniteCache"));

        String client = igniteCache.stringValue("client").orElse("default");
        String name = igniteCache.stringValue("value").orElseThrow(() -> new IllegalStateException("Missing value for cache"));

        IgniteClient igniteClient = beanContext.findBean(IgniteClient.class, Qualifiers.byName(client))
            .orElseThrow(() -> new IllegalStateException("Failed to find bean" + client));
        return igniteClient.cache(name);
    }
}
