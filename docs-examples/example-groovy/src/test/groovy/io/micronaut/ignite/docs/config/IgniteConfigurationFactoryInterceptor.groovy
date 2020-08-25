package io.micronaut.ignite.docs.config

import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import org.apache.ignite.configuration.IgniteConfiguration

@Singleton
class IgniteConfigurationFactoryInterceptor implements BeanCreatedEventListener<IgniteConfiguration>{
    @Override
    IgniteConfiguration onCreated(BeanCreatedEvent<IgniteConfiguration> event) {
        IgniteConfiguration configuration = event.bean
        configuration.setIgniteInstanceName("instance-a")
        return configuration
    }
}
