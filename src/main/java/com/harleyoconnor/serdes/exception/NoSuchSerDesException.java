package com.harleyoconnor.serdes.exception;

import com.harleyoconnor.serdes.SerDes;

import java.io.Serial;

/**
 * Thrown when a {@link SerDes} that was expected to be registered was not.
 *
 * @author Harley O'Connor
 */
public final class NoSuchSerDesException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6504351148528898348L;

    /**
     * Constructs a {@link NoSuchSerDesException} without a detail message.
     */
    public NoSuchSerDesException() {}

    /**
     * Constructs a {@link NoSuchSerDesException} with a detail message.
     *
     * @param message The detail message.
     */
    public NoSuchSerDesException(final String message) {
        super(message);
    }

}
