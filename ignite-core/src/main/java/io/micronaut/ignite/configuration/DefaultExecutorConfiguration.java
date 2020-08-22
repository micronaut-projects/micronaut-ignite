package io.micronaut.ignite.configuration;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.ignite.annotation.IgnitePrimary;
import org.apache.ignite.configuration.ExecutorConfiguration;

@IgnitePrimary
@EachProperty(value = DefaultExecutorConfiguration.PREFIX, list = true)
public class DefaultExecutorConfiguration extends ExecutorConfiguration {
    public static final String PREFIX = DefaultIgniteConfiguration.PREFIX + "." + "executor-configuration";
}
