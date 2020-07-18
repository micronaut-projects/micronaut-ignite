package io.micronaut.ignite.intercept;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import org.apache.ignite.Ignite;
import org.apache.ignite.compute.ComputeTask;
import org.apache.ignite.compute.gridify.Gridify;
import org.apache.ignite.compute.gridify.GridifyArgument;
import org.apache.ignite.compute.gridify.GridifyInterceptor;
import org.apache.ignite.compute.gridify.aop.GridifyArgumentAdapter;
import org.apache.ignite.compute.gridify.aop.GridifyDefaultTask;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.IgnitionEx;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.internal.util.typedef.G;
import org.apache.ignite.resources.TaskSessionResource;

import javax.inject.Singleton;
import java.beans.beancontext.BeanContext;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.apache.ignite.IgniteState.STARTED;

@Singleton
public class GridfyInterceptor implements MethodInterceptor<Object,Object> {

    private final BeanContext beanContext;


    public GridfyInterceptor(BeanContext beanContext) {
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
//            throw new IgniteCheckedException("Grid is not locally started: " + igniteInstanceName);
        }

        if (G.state(igniteInstanceName) != STARTED) {
//            throw new IgniteCheckedException("Grid is not locally started: " + igniteInstanceName);
        }

        // Initialize defaults.
        Method mtd = context.getTargetMethod();

        GridifyArgument arg = new GridifyArgumentAdapter(mtd.getDeclaringClass(), mtd.getName(),
            mtd.getParameterTypes(), context.getParameterValues(), context.getTarget());

        Class<? extends GridifyInterceptor> value = (Class<? extends GridifyInterceptor>) opt.get().classValue("interceptor").orElse(GridfyInterceptor.class);

        if (!value.equals(GridfyInterceptor.class)) {
//            if(!value.newInstance().isGridify(opt.))
        }

        Ignite ignite = G.ignite(igniteInstanceName);

        int timeout = opt.get().intValue("timeout").orElse(0);
        Optional<String> taskName = opt.get().stringValue("taskName");

        Class<? extends ComputeTask<GridifyArgument, ?>> taskClass = (Class<? extends ComputeTask<GridifyArgument, ?>>) opt.get().classValue("taskClass").orElse(GridifyDefaultTask.class);

        if(!taskClass.equals(GridifyDefaultTask.class)) {
            return ignite.compute().withTimeout(timeout).execute(  (Class<? extends ComputeTask<GridifyArgument, Object>>) taskClass, arg);
        }

        if(!taskName.isPresent()){
            return ignite.compute().withTimeout(timeout).execute(new GridifyDefaultTask(mtd.getDeclaringClass()), arg);
        }
        return ignite.compute().withTimeout(timeout).execute(taskName.get(), arg);

    }
}
