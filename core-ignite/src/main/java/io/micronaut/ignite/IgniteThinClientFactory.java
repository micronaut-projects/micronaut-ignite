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

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.ignite.configuration.ThinClientConfiguration;
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

    @EachBean(ThinClientConfiguration.class)
    public IgniteClient thinClientConfiguration(ThinClientConfiguration configuration) {
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
