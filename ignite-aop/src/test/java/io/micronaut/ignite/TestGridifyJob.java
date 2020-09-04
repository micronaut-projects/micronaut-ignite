package io.micronaut.ignite;

import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.compute.ComputeJobAdapter;
import org.apache.ignite.resources.LoggerResource;

public class TestGridifyJob extends ComputeJobAdapter {
    /**
     *
     */
    @LoggerResource
    private IgniteLogger log;

    /**
     * @param arg Argument.
     */
    public TestGridifyJob(String arg) {
        super(arg);
    }

    @Override
    public Object execute() throws IgniteException {
        if (log.isInfoEnabled())
            log.info("Execute TestGridifyJob.execute(" + argument(0) + ')');

        TestGridifyTarget target = new TestGridifyTarget();

        try {
            if ("1".equals(argument(0)))
                return target.gridifyNonDefaultClass("10");
            else if ("2".equals(argument(0)))
                return target.gridifyNonDefaultName("20");
            else if ("3".equals(argument(0)))
                return target.gridifyNonDefaultClassResource("30");
            else if ("4".equals(argument(0)))
                return target.gridifyNonDefaultNameResource("40");
        } catch (TestGridifyException e) {
            throw new RuntimeException("Failed to execute target method.", e);
        }

        assert false : "Argument must be equals to \"0\" [gridifyArg=" + argument(0) + ']';

        // Never reached.
        return null;
    }
}
