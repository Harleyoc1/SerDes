package com.harleyoconnor.serdes.exception;

import java.sql.SQLException;

/**
 * Thrown when a particular column cannot be found in an SQL database.
 *
 * @author Harley O'Connor
 */
public final class NoSuchColumnException extends SQLException {

    /**
     * Constructs a {@link NoSuchColumnException} without a detail message.
     */
    public NoSuchColumnException() {}

    /**
     * Constructs a {@link NoSuchColumnException} with a detail message.
     *
     * @param message The detail message.
     */
    public NoSuchColumnException(final String message) {
        super(message);
    }

}
