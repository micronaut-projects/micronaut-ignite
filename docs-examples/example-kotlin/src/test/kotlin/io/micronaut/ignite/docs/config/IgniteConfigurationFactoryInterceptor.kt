package io.micronaut.ignite.docs.config

import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import org.apache.ignite.configuration.IgniteConfiguration

class IgniteConfigurationFactoryInterceptor : BeanCreatedEventListener<IgniteConfiguration> {
    override fun onCreated(event: BeanCreatedEvent<IgniteConfiguration>?): IgniteConfiguration {
        val configuration = event!!.bean
        configuration.igniteInstanceName = "instance-a"
        return configuration
    }
}
