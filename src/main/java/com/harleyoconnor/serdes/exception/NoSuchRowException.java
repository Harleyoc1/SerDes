package com.harleyoconnor.serdes.exception;

import java.sql.SQLException;

/**
 * Thrown when a particular row cannot be found in an SQL database.
 *
 * @author Harley O'Connor
 */
public final class NoSuchRowException extends SQLException {

    /**
     * Constructs a {@link NoSuchRowException} without a detail message.
     */
    public NoSuchRowException() {}

    /**
     * Constructs a {@link NoSuchRowException} with a detail message.
     *
     * @param message The detail message.
     */
    public NoSuchRowException(final String message) {
        super(message);
    }

}
