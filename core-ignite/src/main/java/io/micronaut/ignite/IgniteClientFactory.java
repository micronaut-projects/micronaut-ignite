package io.micronaut.ignite;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.ignite.configuration.IgniteClientConfiguration;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Factory
public class IgniteClientFactory implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(IgniteClientFactory.class);
    private List<Ignite> sessions = new ArrayList<>(2);

    @EachBean(IgniteClientConfiguration.class)
    @Bean(preDestroy = "close")
    public Ignite clientConfiguration(IgniteClientConfiguration configuration) {
        Ignite client = Ignition.start(configuration.getConfiguration());
        sessions.add(client);
        return client;
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
