package io.micronaut.ignite.configuration.ipfinder;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.ignite.annotation.IgnitePrimary;
import io.micronaut.ignite.configuration.DefaultIgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder;

@IgnitePrimary
@ConfigurationProperties(value = DefaultTcpDiscoveryKubernetesIpFinder.PREFIX)
@Requires(property = DefaultTcpDiscoveryKubernetesIpFinder.PREFIX + "." + "enabled", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class DefaultTcpDiscoveryKubernetesIpFinder extends TcpDiscoveryKubernetesIpFinder implements Toggleable {
    public static final String PREFIX = DefaultIgniteConfiguration.PREFIX_DISCOVERY + ".kubernetes-ip-finder";
    private boolean isEnabled;

    public DefaultTcpDiscoveryKubernetesIpFinder() {
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
