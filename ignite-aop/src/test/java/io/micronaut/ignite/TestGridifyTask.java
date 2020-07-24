package io.micronaut.ignite;

import org.apache.ignite.IgniteException;
import org.apache.ignite.compute.ComputeJob;
import org.apache.ignite.compute.ComputeJobResult;
import org.apache.ignite.compute.ComputeTaskName;
import org.apache.ignite.compute.ComputeTaskSplitAdapter;
import org.apache.ignite.compute.gridify.GridifyArgument;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ComputeTaskName(TestGridifyTask.TASK_NAME)
public class TestGridifyTask extends ComputeTaskSplitAdapter<GridifyArgument, Object> {
    /**
     *
     */
    public static final String TASK_NAME = "io.micronaut.ignite.TestGridfyTask";

    @Override
    protected Collection<? extends ComputeJob> split(int gridSize, GridifyArgument arg) throws IgniteException {
        assert arg.getMethodParameters().length == 1;

        return Collections.singletonList(new TestGridifyJob((String) arg.getMethodParameters()[0]));
    }

    @Override
    public Object reduce(List<ComputeJobResult> results) throws IgniteException {
        assert results.size() == 1;

        return results.get(0).getData();
    }
}
