package io.micronaut.ignite;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.ignite.configuration.ClientConfiguration;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Factory
public class IgniteClientFactory implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(IgniteClientFactory.class);

    private final ResourceResolver resourceResolver;

    public IgniteClientFactory(ResourceResolver resourceResolver){
        this.resourceResolver = resourceResolver;
    }

    @EachBean(ClientConfiguration.class)
    public Ignite clientConfiguration(ClientConfiguration configuration) throws Exception {
        try {
            Optional<URL> template = resourceResolver.getResource(configuration.getPath());
            if(!template.isPresent())
                throw new Exception("failed to find configuration: " + configuration.getPath());
            return Ignition.start(template.get());
        } catch (Exception e) {
            LOG.error("Failed to instantiate Ignite: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void close() {

        Ignition.stopAll(true);
    }
}
