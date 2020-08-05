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
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.http.scope.RequestScope;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.net.URL;
import java.util.Optional;

@Factory
@Requires(property = IgniteClientConfiguration.PREFIX + ".enabled", value = "true", defaultValue = "false")
public class IgniteFactory implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(IgniteFactory.class);

    private final ResourceResolver resourceResolver;
    private final BeanContext beanContext;

    public IgniteFactory(ResourceResolver resourceResolver, BeanContext beanContext) {
        this.resourceResolver = resourceResolver;
        this.beanContext = beanContext;
    }

    @Singleton
    @EachBean(IgniteClientConfiguration.class)
    @Bean(preDestroy = "close")
    public Ignite ignite(IgniteClientConfiguration configuration) {
        try {
            Optional<URL> template = resourceResolver.getResource(configuration.getPath());
            if (!template.isPresent()) {
                throw new RuntimeException("failed to find configuration: " + configuration.getPath());
            }
            return Ignition.start(template.get());
        } catch (Exception e) {
            LOG.error("Failed to instantiate Ignite: " + e.getMessage(), e);
            throw e;
        }
    }


    @Singleton
    @EachBean(IgniteConfiguration.class)
    @Bean(preDestroy = "close")
    public Ignite ignite(IgniteConfiguration configuration) {
        try {
            return Ignition.start(configuration);
        } catch (Exception e) {
            LOG.error("Failed to instantiate Ignite Client: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get {@link IgniteCache} from parameter annotated with {@link IgniteRef}.
     *
     * @param injectionPoint injection point for {@link IgniteCache}.
     * @return An instance of the {@link ClientCache} from {@link Ignite}.
     */
    @Prototype
    @Bean
    protected IgniteCache igniteCache(InjectionPoint<?> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        return resolveIgniteCache(metadata);
    }

    public IgniteCache resolveIgniteCache(AnnotationMetadata metadata) {
        AnnotationValue<IgniteRef> igniteCache = metadata.findAnnotation(IgniteRef.class)
            .orElseThrow(() -> new IllegalStateException("Requires @IgniteCache"));
        String client = igniteCache.stringValue("client").orElse("default");
        String name = igniteCache.stringValue("value").orElseThrow(() -> new IllegalStateException("Missing value for cache"));
        Ignite ignite = beanContext.getBean(Ignite.class, Qualifiers.byName(client));

        return ignite.getOrCreateCache(name);
    }

    @RequestScope
    @Bean
    public IgniteDataStreamer igniteDataStreamer(InjectionPoint<?> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        return resolveDataStream(metadata);
    }

    public IgniteDataStreamer resolveDataStream(AnnotationMetadata metadata) {
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
