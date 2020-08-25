package io.micronaut.ignite.configuration.ipfinder;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.ignite.annotation.IgnitePrimary;
import io.micronaut.ignite.configuration.DefaultIgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import java.util.Collection;

@IgnitePrimary
@ConfigurationProperties(value = DefaultDiscoveryVmIpFinder.PREFIX)
@Requires(property = DefaultDiscoveryVmIpFinder.PREFIX + "." + "enabled", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class DefaultDiscoveryVmIpFinder extends TcpDiscoveryVmIpFinder implements Toggleable {
    public static final String PREFIX = DefaultIgniteConfiguration.PREFIX_DISCOVERY + ".static-ip-finder";
    private boolean isEnabled;

    public DefaultDiscoveryVmIpFinder() {
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @EachProperty(value = "addresses")
    public void setStaticIpFinderAddresses(Collection<String> addresses) {
        this.setAddresses(addresses);
    }
}
