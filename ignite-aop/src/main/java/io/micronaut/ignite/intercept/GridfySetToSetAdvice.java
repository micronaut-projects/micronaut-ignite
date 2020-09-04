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
import io.micronaut.core.reflect.exception.InvocationException;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.compute.gridify.GridifyInterceptor;
import org.apache.ignite.compute.gridify.GridifyNodeFilter;
import org.apache.ignite.compute.gridify.GridifyRuntimeException;
import org.apache.ignite.compute.gridify.GridifySetToSet;
import org.apache.ignite.compute.gridify.aop.GridifySetToSetAbstractAspect;
import org.apache.ignite.internal.util.gridify.GridifyArgumentBuilder;
import org.apache.ignite.internal.util.gridify.GridifyRangeArgument;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.internal.util.typedef.G;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.apache.ignite.IgniteState.STARTED;
import static org.apache.ignite.internal.util.gridify.GridifyUtils.UNKNOWN_SIZE;

public class GridfySetToSetAdvice extends GridifySetToSetAbstractAspect implements MethodInterceptor<Object, Object> {

    private final BeanContext beanContext;

    public GridfySetToSetAdvice(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        Optional<AnnotationValue<GridifySetToSet>> opt = context.findAnnotation(GridifySetToSet.class);
        if (!opt.isPresent()) {
            return context.proceed();
        }
        String igniteInstanceName = opt.get().stringValue("igniteInstanceName").orElse(opt.get().stringValue("gridName").orElse(null));

        if (F.isEmpty(igniteInstanceName)) {
            throw new InvocationException("Grid is not locally started: " + igniteInstanceName, new IgniteCheckedException());
        }

        if (G.state(igniteInstanceName) != STARTED) {
            throw new InvocationException("Grid is not locally started: " + igniteInstanceName, new IgniteCheckedException());
        }


        GridifyNodeFilter nodeFilter = null;
        Class<? extends GridifyNodeFilter> nodeFilterClazz = (Class<? extends GridifyNodeFilter>) opt.get().classValue("nodeFilter").orElse(GridifyNodeFilter.class);
        if (!nodeFilterClazz.equals(GridifyNodeFilter.class)) {
            nodeFilter = beanContext.getBean(nodeFilterClazz);
        }


        GridifyArgumentBuilder argBuilder = new GridifyArgumentBuilder();
        Method mtd = context.getTargetMethod();

        GridifyRangeArgument arg = argBuilder.createTaskArgument(
            context.getDeclaringType(),
            context.getName(),
            context.getReturnType().getClass(),
            context.getArgumentTypes(),
            mtd.getParameterAnnotations(),
            context.getParameterValues(),
            context.getTarget()
        );

        Class<? extends GridifyInterceptor> gridifyInterceptorClazz = (Class<? extends GridifyInterceptor>) opt.get().classValue("interceptor").orElse(GridifyInterceptor.class);
        if (!gridifyInterceptorClazz.equals(GridifyInterceptor.class)) {
            GridifyInterceptor interceptor = beanContext.getBean(gridifyInterceptorClazz);
            try {
                if (!interceptor.isGridify(mtd.getAnnotation(GridifySetToSet.class), arg)) {
                    return context.proceed();
                }
            } catch (IgniteCheckedException e) {
                throw new RuntimeException(e);
            }
        }

        int threshold = opt.get().intValue("threshold").orElse(0);
        int splitSize = opt.get().intValue("splitSize").orElse(0);
        int timeout = opt.get().intValue("timeout").orElse(0);
        if (threshold < 0) {
            context.proceed();
        }


        if (arg.getInputSize() != UNKNOWN_SIZE && arg.getInputSize() <= threshold) {
            return context.proceed();
        }

        // Check is split to jobs allowed for input method argument with declared splitSize.
        //  checkIsSplitToJobsAllowed(arg, ann);
        if (arg.getInputSize() == UNKNOWN_SIZE && threshold <= 0 && splitSize <= 0) {
            throw new InvocationException("Failed to split input method argument to jobs with unknown input size and " +
                "invalid annotation parameter 'splitSize' [mtdName=" + arg.getMethodName() + ", inputTypeCls=" +
                arg.getMethodParameterTypes()[arg.getParamIndex()].getName() +
                ", threshold=" + threshold + ", splitSize=" + splitSize + ']', new IgniteCheckedException());
        }

        try {
            Ignite ignite = G.ignite(igniteInstanceName);

            return execute(ignite.compute(), context.getDeclaringType(), arg, nodeFilter, threshold, splitSize, timeout);
        } catch (IgniteCheckedException e) {

            for (Class<?> ex : mtd.getExceptionTypes()) {
                // Descend all levels down.
                Throwable cause = e.getCause();

                while (cause != null) {
                    if (ex.isAssignableFrom(cause.getClass())) {
                        throw new InvocationException("Failed to execute Gridify Set ", cause);
                    }

                    cause = cause.getCause();
                }

                if (ex.isAssignableFrom(e.getClass())) {
                    throw new InvocationException("invocation exception thrown: ", e);
                }
            }
            throw new GridifyRuntimeException("Undeclared exception thrown: " + e.getMessage(), e);
        }
    }
}
