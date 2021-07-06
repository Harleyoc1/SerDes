package com.harleyoconnor.serdes.exception;

import java.io.Serial;

/**
 * Thrown to indicate that an annotation has been passed an illegal or
 * inappropriate element.
 *
 * @author Harley O'Connor
 * @since SerDes 1.0.0
 */
// TODO: Move to JavaUtilities
public final class IllegalElementException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4054520030969859220L;

    /**
     * Constructs an {@code IllegalElementException} with no
     * detail message.
     */
    public IllegalElementException() {
        super();
    }

    /**
     * Constructs an {@code IllegalElementException} with the
     * specified detail message.
     *
     * @param s The detail message.
     */
    public IllegalElementException(String s) {
        super(s);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     *
     * <p>Note that the detail message associated with {@code cause} is
     * <i>not</i> automatically incorporated in this exception's detail
     * message.
     *
     * @param message The detail message (which is saved for later retrieval
     *                by the {@link Throwable#getMessage()} method).
     * @param cause The cause (which is saved for later retrieval by the
     *              {@link Throwable#getCause()} method).  (A {@code null} value
     *              is permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    public IllegalElementException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of {@code (cause==null ? null : cause.toString())} (which
     * typically contains the class and detail message of {@code cause}).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * java.security.PrivilegedActionException}).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link Throwable#getCause()} method).  (A {@code null} value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    public IllegalElementException(Throwable cause) {
        super(cause);
    }

}
