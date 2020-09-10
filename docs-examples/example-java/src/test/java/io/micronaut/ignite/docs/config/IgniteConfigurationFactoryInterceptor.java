package io.micronaut.ignite.docs.config;

import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import org.apache.ignite.configuration.IgniteConfiguration;

import javax.inject.Singleton;

@Singleton
public class IgniteConfigurationFactoryInterceptor implements BeanCreatedEventListener<IgniteConfiguration> {
    @Override
    public IgniteConfiguration onCreated(BeanCreatedEvent<IgniteConfiguration> event) {
        IgniteConfiguration configuration = event.getBean();
        configuration.setIgniteInstanceName("instance-a");
        return configuration;
    }
}
