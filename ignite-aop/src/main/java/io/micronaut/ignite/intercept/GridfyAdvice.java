package io.micronaut.ignite.intercept;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.reflect.exception.InvocationException;
import io.micronaut.inject.ExecutableMethod;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.compute.ComputeTask;
import org.apache.ignite.compute.gridify.Gridify;
import org.apache.ignite.compute.gridify.GridifyArgument;
import org.apache.ignite.compute.gridify.GridifyInterceptor;
import org.apache.ignite.compute.gridify.aop.GridifyArgumentAdapter;
import org.apache.ignite.compute.gridify.aop.GridifyDefaultTask;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.internal.util.typedef.G;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.apache.ignite.IgniteState.STARTED;

@Singleton
public class GridfyAdvice implements MethodInterceptor<Object,Object> {

    private final BeanContext beanContext;


    public GridfyAdvice(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        Optional<AnnotationValue<Gridify>> opt = context.findAnnotation(Gridify.class);
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

        // Initialize defaults.
        ExecutableMethod<Object, Object> mtd = context.getExecutableMethod();

        GridifyArgument arg = new GridifyArgumentAdapter(context.getTarget().getClass(), mtd.getName(),
            context.getArgumentTypes(), context.getParameterValues(), context.getTarget());

        Class<? extends GridifyInterceptor> value = (Class<? extends GridifyInterceptor>) opt.get().classValue("interceptor").orElse(GridifyInterceptor.class);

        if (!value.equals(GridifyInterceptor.class)) {
            GridifyInterceptor interceptor = beanContext.getBean(value);
            Annotation annotation = context.getTargetMethod().getAnnotation(Gridify.class);
            try {
                if (!interceptor.isGridify(annotation, arg))
                    return context.proceed();
            } catch (IgniteCheckedException e) {
                throw new RuntimeException(e);
            }
        }

        Ignite ignite = G.ignite(igniteInstanceName);

        int timeout = opt.get().intValue("timeout").orElse(0);
        Optional<String> taskName = opt.get().stringValue("taskName");

        Class<? extends ComputeTask<GridifyArgument, ?>> taskClass = (Class<? extends ComputeTask<GridifyArgument, ?>>) opt.get().classValue("taskClass").orElse(GridifyDefaultTask.class);

        if (!taskClass.equals(GridifyDefaultTask.class)) {
            return ignite.compute().withTimeout(timeout).execute((Class<? extends ComputeTask<GridifyArgument, Object>>) taskClass, arg);
        }

        if (!taskName.isPresent()) {
            return ignite.compute().withTimeout(timeout).execute(new GridifyDefaultTask(context.getTarget().getClass()), arg);
        }
        return ignite.compute().withTimeout(timeout).execute(taskName.get(), arg);
    }
}
