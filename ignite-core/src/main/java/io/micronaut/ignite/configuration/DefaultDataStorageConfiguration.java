package io.micronaut.ignite.configuration;

import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.ignite.annotation.IgnitePrimary;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;

import java.util.Collection;

@IgnitePrimary
@ConfigurationProperties(value = DefaultDataStorageConfiguration.PREFIX, excludes = {"data-region-configurations"})
@Requires(property = DefaultDataStorageConfiguration.PREFIX + "." + "enabled", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class DefaultDataStorageConfiguration extends DataStorageConfiguration implements Toggleable {
    public static final String PREFIX = DefaultIgniteConfiguration.PREFIX + "." + "data-storage-configuration";
    private boolean isEnabled;

    @ConfigurationBuilder("default-data-region-configuration")
    final DataRegionConfiguration defaultDataRegionConfiguration = new DataRegionConfiguration();

    public DefaultDataStorageConfiguration() {
        super.setDefaultDataRegionConfiguration(defaultDataRegionConfiguration);
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @EachProperty(value = "regions", list = true)
    public void setRegions(Collection<DataRegionConfiguration> regions) {
        super.setDataRegionConfigurations(regions.toArray(new DataRegionConfiguration[0]));
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
