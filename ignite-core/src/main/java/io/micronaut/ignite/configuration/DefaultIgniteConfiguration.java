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
package io.micronaut.ignite.configuration;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.naming.Named;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;

import java.util.Collection;
import java.util.List;

/**
 * Ignite cache configuration.
 */
@ConfigurationProperties(value = DefaultIgniteConfiguration.PREFIX)
public class DefaultIgniteConfiguration {
    public static final String PREFIX = "ignite";

    @ConfigurationBuilder(excludes = "cacheConfiguration")
    private IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
    @ConfigurationBuilder(value = "communicationSpi")
    private TcpCommunicationSpi communicationSpi = new TcpCommunicationSpi();

    public TcpCommunicationSpi getCommunicationSpi() {
        return communicationSpi;
    }

    public void setCommunicationSpi(TcpCommunicationSpi communicationSpi) {
        this.communicationSpi = communicationSpi;
    }

    public IgniteConfiguration getIgniteConfiguration() {
        return igniteConfiguration;
    }

    public void setIgniteConfiguration(IgniteConfiguration igniteConfiguration) {
        this.igniteConfiguration = igniteConfiguration;
    }

    private String path;

    /**
     * path to load in bean configuration.
     *
     * @param path bean config to load.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * path to load in bean configuration.
     *
     * @return bean config to load.
     */
    public String getPath() {
        return path;
    }

    public IgniteConfiguration getConfiguration(){
        return new IgniteConfiguration(this.getIgniteConfiguration())
            .setCommunicationSpi(this.communicationSpi);
    }

}
