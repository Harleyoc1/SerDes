package com.harleyoconnor.serdes.database;

import com.harleyoconnor.serdes.util.Scheduler;

import java.sql.Connection;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * The default {@link Database} to fallback to if no {@link Database} is
 * specified.
 *
 * <p>This is particularly useful for applications where only one database
 * is used, as it means there is no longer any need to pass around
 * {@link Database} instances.</p>
 *
 * @author Harley O'Connor
 * @since 0.0.5
 */
public final class DefaultDatabase extends Database {

    private static DefaultDatabase DEFAULT_DATABASE;

    /**
     * Gets the {@link #DEFAULT_DATABASE}.
     *
     * @return The {@link #DEFAULT_DATABASE} object.
     */
    public static DefaultDatabase get() {
        return DEFAULT_DATABASE;
    }

    /**
     * Creates and sets the {@link #DEFAULT_DATABASE} to the specified
     * {@link Connection}.
     *
     * <p>Takes a {@link Supplier} since the {@link Connection} is
     * reset every 15 minutes.</p>
     *
     * @param connectionSupplier A {@link Supplier} for the
     *                           {@link Connection} to the
     *                           {@link #DEFAULT_DATABASE}.
     */
    public static void set(final Supplier<Connection> connectionSupplier) {
        DEFAULT_DATABASE = new DefaultDatabase(connectionSupplier.get());

        Scheduler.schedule(() -> DEFAULT_DATABASE =
                        new DefaultDatabase(connectionSupplier.get()),
                Duration.ofMinutes(15));
    }

    /**
     * Constructs a new {@link DefaultDatabase} with the specified
     * {@link Connection}.
     *
     * @param connection The SQL database {@link Connection}.
     */
    private DefaultDatabase(final Connection connection) {
        super(connection);
    }

}
