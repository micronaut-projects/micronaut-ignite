/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.ignite.intercept;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.ReturnType;
import io.micronaut.ignite.annotation.IgniteTransaction;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.reactivex.Flowable;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteTransactions;
import org.apache.ignite.transactions.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public class IgniteTransactionInterceptor implements MethodInterceptor<Object,Object> {
    private static final Logger LOG = LoggerFactory.getLogger(IgniteTransactionInterceptor.class);
    private BeanContext beanContext;

    public IgniteTransactionInterceptor(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public Object intercept(MethodInvocationContext context) {
        Optional<AnnotationValue<IgniteTransaction>> opt = context.findAnnotation(IgniteTransaction.class);
        if (!opt.isPresent()) {
            return context.proceed();
        }
        AnnotationValue<IgniteTransaction> transaction = opt.get();
        String clientId = transaction.stringValue("client").orElse("default");
        Ignite ignite = beanContext.getBean(Ignite.class, Qualifiers.byName(clientId));
        IgniteTransactions txHandler = ignite.transactions();

        try (Transaction tx = txHandler.txStart()) {
            ReturnType<Object> returnType = context.getReturnType();
            Class<Object> javaReturnType = returnType.getType();
            if (CompletionStage.class.isAssignableFrom(javaReturnType)) {
                Object result = context.proceed();
                if (result == null) {
                    return result;
                } else {
                    CompletableFuture newFuture = new CompletableFuture();
                    ((CompletableFuture<?>) result).whenComplete((value, exception) -> {
                        if (exception == null) {
                            tx.commit();
                            newFuture.complete(value);
                            return;
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Cannot retry anymore. Rethrowing original exception for method: {}", context);
                        }
                        tx.rollback();
                        newFuture.completeExceptionally(exception);
                    });
                    return newFuture;
                }
            } else if (Publishers.isConvertibleToPublisher(returnType)) {
                ConversionService<?> conversionService = ConversionService.SHARED;
                Object result = context.proceed();
                if (result == null) {
                    return result;
                } else {
                    Flowable observable = conversionService
                        .convert(result, Flowable.class)
                        .orElseThrow(() -> new IllegalStateException("Unconvertible Reactive type: " + result));
                    Flowable retryObservable = observable.doOnError((throwable -> {
                        tx.rollback();
                    })).doOnComplete(() -> {
                        tx.commit();
                    });
                    return conversionService
                        .convert(retryObservable, returnType.asArgument())
                        .orElseThrow(() -> new IllegalStateException("Unconvertible Reactive type: " + result));
                }
            }
            Object res = context.proceed();
            tx.commit();
            return res;
        }
    }
}
