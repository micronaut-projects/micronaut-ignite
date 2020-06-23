package io.micronaut.ignite;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.ignite.configuration.IgniteClientConfiguration;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.lucene.analysis.util.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Factory
public class IgniteClientFactory implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(IgniteClientFactory.class);
    private List<Ignite> sessions = new ArrayList<>(2);

    private final ResourceResolver resourceResolver;

    public IgniteClientFactory(ResourceResolver resourceResolver){
        this.resourceResolver = resourceResolver;
    }

    @EachBean(IgniteClientConfiguration.class)
    public Ignite clientConfiguration(IgniteClientConfiguration configuration) throws Exception {
        try {
            Optional<URL> template = resourceResolver.getResource(configuration.getPath());
            if(!template.isPresent())
                throw new Exception("failed to find configuration: " + configuration.getPath());
            Ignite client = Ignition.start(template.get());
            sessions.add(client);
            return client;
        } catch (Exception e) {
            LOG.error("Failed to instantiate Ignite: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void close() {
        for (Ignite sess : sessions) {
            try {
                sess.close();
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Error closing ignite [" + sess + "]: " + e.getMessage(), e);
                }
            }
        }
    }
}
