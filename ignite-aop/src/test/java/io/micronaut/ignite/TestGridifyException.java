package io.micronaut.ignite;

public class TestGridifyException extends RuntimeException {
    /**
     * @param msg Message.
     */
    public TestGridifyException(String msg) {
        super(msg);
    }

    /**
     * @param msg   Message.
     * @param cause Exception cause.
     */
    public TestGridifyException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
