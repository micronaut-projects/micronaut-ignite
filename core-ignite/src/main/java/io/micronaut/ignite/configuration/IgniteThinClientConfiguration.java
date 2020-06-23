package io.micronaut.ignite.configuration;

import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.naming.Named;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.configuration.ClientTransactionConfiguration;

import javax.annotation.Nonnull;

@EachProperty(value = IgniteThinClientConfiguration.PREFIX, primary = "default")
public class IgniteThinClientConfiguration implements Named {
    public static final String PREFIX = IgniteConfig.PREFIX + "." + "thin-client";

    private final String name;

    @ConfigurationBuilder(excludes = {"transactionConfiguration", "binaryConfiguration", "sslContextFactory"})
    private final ClientConfiguration configuration = new ClientConfiguration();

    /**
     * @param name Name or key of the client.
     */
    public IgniteThinClientConfiguration(@Parameter String name, TransactionConfiguration transaction) {
        this.name = name;
        configuration.setTransactionConfiguration(transaction.getConfiguration());
    }

    @ConfigurationProperties("transaction")
    public static class TransactionConfiguration{
        @ConfigurationBuilder(value = "transactionConfiguration")
        private final ClientTransactionConfiguration configuration = new ClientTransactionConfiguration();

        public ClientTransactionConfiguration getConfiguration() {
            return configuration;
        }
    }

    public ClientConfiguration getConfiguration() {
        return configuration;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }
}
