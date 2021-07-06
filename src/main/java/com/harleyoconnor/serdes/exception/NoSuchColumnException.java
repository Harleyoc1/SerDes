package com.harleyoconnor.serdes.exception;

import java.io.Serial;
import java.sql.SQLException;

/**
 * Thrown when a particular column cannot be found in an SQL database.
 *
 * @author Harley O'Connor
 */
public final class NoSuchColumnException extends SQLException {

    @Serial
    private static final long serialVersionUID = -1399208132131786078L;

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
