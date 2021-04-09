package com.harleyoconnor.serdes;

import com.harleyoconnor.serdes.exception.NoSuchSerDesException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Holds a central registry for {@link SerDes} objects.
 *
 * @author Harley O'Connor
 */
@SuppressWarnings("unchecked")
public final class SerDesRegistry {

    /**
     * Why would you ever need an instance of this class anyway?
     */
    private SerDesRegistry() {}

    /** The {@link Set} of {@link SerDes} objects registered. */
    private static final Set<SerDes<?, ?>> serDesables = new HashSet<>();

    public static <T extends SerDesable<T, ?>> SerDes<T, ?> getUnsafe (final Class<T> serDesableClass) throws NoSuchSerDesException {
        return get(serDesableClass).orElseThrow(() -> new NoSuchSerDesException("No SerDes found for Class '" + serDesableClass.getSimpleName() + "'."));
    }

    public static <T extends SerDesable<T, ?>> Optional<SerDes<T, ?>> get (final Class<T> serDesableClass) {
        return (Optional<SerDes<T, ?>>) getOptionalCapture(serDesableClass);
    }

    private static <T extends SerDesable<T, ?>> Optional<? extends SerDes<T, ?>> getOptionalCapture(final Class<T> serDesableClass) {
        return serDesables.stream().filter(serDes -> serDes.getType().equals(serDesableClass)).map(serDes -> (SerDes<T, ?>) serDes).findFirst();
    }

    public static <T extends SerDesable<T, ?>> void register (final SerDes<T, ?> serDes) {
        serDesables.add(serDes);
    }

}
