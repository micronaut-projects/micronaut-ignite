package io.micronaut.ignite;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.ignite.configuration.IgniteThinClientConfiguration;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.IgniteClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Factory
public class IgniteThinClientFactory implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(IgniteThinClientFactory.class);
    private final List<IgniteClient> sessions = new ArrayList<>(2);

    @EachBean(IgniteThinClientConfiguration.class)
    public IgniteClient thinClientConfiguration(IgniteThinClientConfiguration configuration) {
        IgniteClient client = Ignition.startClient(configuration.getConfiguration());
        sessions.add(client);
        return client;
    }

    @Override
    public void close() {
        for (IgniteClient sess : sessions) {
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
