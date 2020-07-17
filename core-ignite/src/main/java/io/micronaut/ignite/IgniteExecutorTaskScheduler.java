package io.micronaut.ignite;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.util.StringUtils;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.TaskScheduler;
import org.apache.ignite.Ignite;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Named;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.micronaut.core.util.ArgumentUtils.check;

@Named("ignite")
@Primary
public class IgniteExecutorTaskScheduler implements TaskScheduler {

    private final Ignite ignite;
    private final ExecutorService executorService;

    public IgniteExecutorTaskScheduler(@Primary Ignite ignite, @Named(TaskExecutors.SCHEDULED) ExecutorService executorService) {
        this.ignite = ignite;
        this.executorService = executorService;
    }

    @Override
    public ScheduledFuture<?> schedule(String cron, Runnable command) {
        if (StringUtils.isEmpty(cron)) {
            throw new IllegalArgumentException("Blank cron expression not allowed");
        }
        check("command", command).notNull();

        ignite.scheduler().scheduleLocal(command, cron);
        return null;
    }

    @Override
    public <V> ScheduledFuture<V> schedule(String cron, Callable<V> command) {
        if (StringUtils.isEmpty(cron)) {
            throw new IllegalArgumentException("Blank cron expression not allowed");
        }
        check("command", command).notNull();

        ignite.scheduler().scheduleLocal(command, cron);
        return null;
    }

    @Override
    public ScheduledFuture<?> schedule(Duration delay, Runnable command) {
        check("delay", delay).notNull();
        check("command", command).notNull();

        ignite.scheduler().runLocal(command, delay.toMillis(), TimeUnit.MILLISECONDS);
        return null;
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Duration delay, Callable<V> callable) {
        check("delay", delay).notNull();

        ignite.scheduler().callLocal(callable);
        return null;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(@Nullable Duration initialDelay, Duration period, Runnable command) {
        throw new NotImplementedException();

    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(@Nullable Duration initialDelay, Duration delay, Runnable command) {
        throw new NotImplementedException();
    }
}
