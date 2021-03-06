package com.harleyoconnor.serdes.exception;

/**
 * Thrown when a particular {@link java.lang.reflect.Constructor} cannot be found.
 *
 * @author Harley O'Connor
 */
public final class NoSuchConstructorException extends NoSuchMethodException {

    /**
     * Constructs a {@link NoSuchMethodException} without a detail message.
     */
    public NoSuchConstructorException() {
    }

    /**
     * Constructs a {@link NoSuchConstructorException} with a detail message.
     *
     * @param message The detail message.
     */
    public NoSuchConstructorException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link NoSuchConstructorException} from the given
     * {@link NoSuchMethodException}.
     *
     * @param e The {@link NoSuchMethodException} to convert.
     * @return The constructed {@link NoSuchConstructorException}.
     */
    public static NoSuchConstructorException from(final NoSuchMethodException e) {
        return new NoSuchConstructorException(e.getMessage());
    }

}
