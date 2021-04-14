package io.micronaut.ignite.intercept;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.core.type.Argument;
import io.micronaut.ignite.annotation.PubSubListener;
import io.micronaut.ignite.annotation.Subscription;
import io.micronaut.ignite.annotation.Topic;
import io.micronaut.ignite.bind.PubSubBinderRegistry;
import io.micronaut.ignite.bind.PubSubConsumerState;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.messaging.exceptions.MessageListenerException;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteMessaging;
import org.apache.ignite.stream.StreamReceiver;

import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of an {@link ExecutableMethodProcessor} that creates
 * {@link IgniteDataStreamer}
 */
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
        io.micronaut.context.Qualifier<Object> qualifer = beanDefinition
            .getAnnotationTypeByStereotype(Qualifier.class)
            .map(type -> Qualifiers.byAnnotation(beanDefinition, type))
            .orElse(null);

        if (subscriptionAnnotation != null) {
//            IgniteMessaging messaging = ignite.message();
            AnnotationValue<Topic> topicAnnotation = method.getAnnotation(Topic.class);
            String topic = topicAnnotation.stringValue().get();

            Class<Object> beanType = (Class<Object>) beanDefinition.getBeanType();
            Object bean = beanContext.findBean(beanType, qualifer).orElseThrow(() -> new MessageListenerException("Could not find the bean to execute the method " + method));
            DefaultExecutableBinder<PubSubConsumerState> binder = new DefaultExecutableBinder<>();

            if (subscriptionAnnotation != null) {
                String subscriptionName = subscriptionAnnotation.getRequiredValue(String.class);
                try (IgniteDataStreamer stmr = ignite.dataStreamer(subscriptionName)) {
                    stmr.receiver((StreamReceiver<?,?>) (cache, collection) -> {
                        for(Map.Entry<?,?> entry: collection) {
                            PubSubConsumerState consumerState = new PubSubConsumerState(cache, entry.getKey(), entry.getValue());
                            BoundExecutable executable = null;
                            try {
                                executable = binder.bind(method, binderRegistry, consumerState);
                            } catch (Exception ex) {
//                                handleException(new PubSubMessageReceiverException("Error binding message to the method", ex, bean, consumerState));
                            }
                            executable.invoke(bean);
                        }
                    });
                }
            }

        }
    }
}
