package io.micronaut.ignite.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.ignite.annotation.IgnitePrimary;
import org.apache.ignite.spi.encryption.keystore.KeystoreEncryptionSpi;

@IgnitePrimary
@ConfigurationProperties(value = DefaultEncryptionSpi.PREFIX)
@Requires(property = DefaultEncryptionSpi.PREFIX + "." + "enabled", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class DefaultEncryptionSpi extends KeystoreEncryptionSpi implements Toggleable {
    public static final String PREFIX = DefaultIgniteConfiguration.PREFIX + ".encryption-spi";

    private boolean isEnabled;

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
