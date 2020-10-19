package io.micronaut.ignite.intercept;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.ignite.annotation.PubSubListener;
import io.micronaut.ignite.annotation.Subscription;
import io.micronaut.ignite.annotation.Topic;
import io.micronaut.ignite.bind.PubSubBinderRegistry;
import io.micronaut.ignite.bind.PubSubConsumerState;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteMessaging;

import javax.inject.Singleton;

@Singleton
public class PubSubConsumerAdvice implements ExecutableMethodProcessor<PubSubListener> {
    private final BeanContext beanContext;
    private final Ignite ignite;
    private final PubSubBinderRegistry binderRegistry;

    public PubSubConsumerAdvice(@Primary Ignite ignite,
                                PubSubBinderRegistry binderRegistry,
                                BeanContext beanContext) {

        this.beanContext = beanContext;
        this.ignite = ignite;
        this.binderRegistry = binderRegistry;
    }

    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        AnnotationValue<Subscription> subscriptionAnnotation = method.getAnnotation(Subscription.class);
        if (subscriptionAnnotation != null) {
            IgniteMessaging messaging = ignite.message();
            AnnotationValue<Topic> topicAnnotation = method.getAnnotation(Topic.class);
            String topic = topicAnnotation.stringValue().get();

            DefaultExecutableBinder<PubSubConsumerState> binder = new DefaultExecutableBinder<>();

            if (subscriptionAnnotation != null) {
                String subscriptionName = subscriptionAnnotation.getRequiredValue(String.class);
            }

            messaging.remoteListen(topic, (nodeId, msg) -> {

                return false;
            });
        }
    }
}
