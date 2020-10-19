package io.micronaut.ignite.intercept;

import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.type.Argument;
import io.micronaut.ignite.annotation.PubSubClient;
import io.micronaut.ignite.annotation.Topic;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.scheduling.TaskExecutors;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteMessaging;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

@Singleton
public class PubSubClientIntroductionAdvice implements MethodInterceptor<Object,Object> {
    private final Scheduler scheduler;
    private final Ignite ignite;

    public PubSubClientIntroductionAdvice(
        @Named(TaskExecutors.IO) ExecutorService executorService,
        @Primary Ignite ignite
        ) {
        this.scheduler = Schedulers.from(executorService);
        this.ignite = ignite;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        if(context.hasAnnotation(Topic.class)){
            InterceptedMethod interceptedMethod = InterceptedMethod.of(context);
            IgniteMessaging messaging = ignite.message();
            ExecutableMethod method = context.getExecutableMethod();
            AnnotationValue<PubSubClient> client = method.findAnnotation(PubSubClient.class).orElseThrow(
                () -> new IllegalStateException("No @PubSubClient annotation present")
            );
            AnnotationValue<Topic> topicAnnotation = method.findAnnotation(Topic.class).get();
            String topic = topicAnnotation.stringValue().get();
//            messaging.send(topic, );

            Argument[] arguments = context.getArguments();
            if(arguments.length != 1){
                return null;
            }



            Object result = context.proceed();
            messaging.send(topic, result);

            messaging.sendOrdered(topic,result,0);

            return result;
        } else {
            return context.proceed();
        }
    }
}
