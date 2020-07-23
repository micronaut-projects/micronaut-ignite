package io.micronaut.ignite;



import org.apache.ignite.compute.gridify.Gridify;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Singleton
public class TestGridifyTarget {
    /**
     * @param arg Argument.
     * @return Result.
     */
    @Gridify(igniteInstanceName = "default")
    public int gridifyDefault(String arg) {
        return Integer.parseInt(arg);
    }

    /**
     * @param arg Argument.
     * @return Result.
     */
    @Gridify(igniteInstanceName = "default", taskClass = TestGridifyTask.class)
    public int gridifyNonDefaultClass(String arg) {
        return Integer.parseInt(arg);
    }

    /**
     * @param arg Argument.
     * @return Result.
     */
    @Gridify(igniteInstanceName = "default", taskName = TestGridifyTask.TASK_NAME)
    public int gridifyNonDefaultName(String arg) {
        return Integer.parseInt(arg);
    }

    /**
     * @param arg Argument.
     * @return Result.
     */
    @Gridify(igniteInstanceName = "default", taskName = "")
    public int gridifyNoName(String arg) {
        return 0;
    }

    /**
     * @param arg Argument.
     * @return Result.
     * @throws TestGridifyException If failed.
     */
    @Gridify(igniteInstanceName = "default")
    public int gridifyDefaultException(String arg) throws TestGridifyException {
        throw new TestGridifyException(arg);
    }

    /**
     * @param arg Argument.
     * @return Result.
     * @throws TestGridifyException If failed.
     */
    @Gridify(igniteInstanceName = "default")
    public int gridifyDefaultResource(String arg) throws TestGridifyException {
        int res = Integer.parseInt(arg);

        Integer rsrcVal = getResource();

        assert rsrcVal != null;
        assert rsrcVal == res : "Invalid result [res=" + res + ", rsrc=" + rsrcVal + ']';

        return res;
    }

    /**
     * @param arg Argument.
     * @return Result.
     * @throws TestGridifyException If failed.
     */
    @Gridify(igniteInstanceName = "default", taskClass = TestGridifyTask.class)
    public int gridifyNonDefaultClassResource(String arg) throws TestGridifyException {
        assert getResource() != null;

        return Integer.parseInt(arg);
    }


    /**
     * @param arg Argument.
     * @return Result.
     * @throws TestGridifyException If failed.
     */
    @Gridify(igniteInstanceName = "default", taskName = TestGridifyTask.TASK_NAME)
    public int gridifyNonDefaultNameResource(String arg) throws TestGridifyException {
        assert getResource() != null;

        return Integer.parseInt(arg);
    }

    /**
     * @return Result.
     * @throws TestGridifyException If failed.
     */
    private Integer getResource() throws TestGridifyException {
        try (InputStream in = getClass().getResourceAsStream("test_resource.properties")) {
            assert in != null;

            Properties prop = new Properties();

            prop.load(in);

            String val = prop.getProperty("param1");

            return Integer.parseInt(val);
        } catch (IOException e) {
            throw new TestGridifyException("Failed to test load properties file.", e);
        }
    }
}
