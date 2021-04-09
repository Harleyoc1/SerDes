package com.harleyoconnor.serdes.util;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides useful helper functions for common SQL tasks.
 *
 * @author Harley O'Connor
 */
public final class SQLHelper {

    /**
     * Gets the connection for the given parameters, or throws a {@link RuntimeException} with the thrown
     * {@link SQLException} if it failed.
     *
     * @param databaseType The type of database for the connection (such as {@code mariadb} for MariaDB).
     * @param ip The IP for the database.
     * @param port The port for the database.
     * @param schema The schema to connect to.
     * @param username The username for the login.
     * @param password The password for the login.
     * @return The {@link Connection} instance.
     */
    public static Connection getConnectionUnsafe (final String databaseType, final String ip, final String port,
                                                  @Nullable final String schema, final String username, final String password) {
        try {
            return DriverManager.getConnection("jdbc:" + databaseType + "://" + ip + ":" + port + (schema != null ? "/" + schema : ""), username, password);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if the given {@link ResultSet} contains the given {@code column}.
     *
     * <p>Note that it may also return {@code false} if there was a database
     * error.</p>
     *
     * @param resultSet The {@link ResultSet} to check.
     * @param column The column name to check for.
     * @return {@code true} if the columns exists and there were no
     *         other database errors, else {@code false}.
     */
    public static boolean containsColumn(final ResultSet resultSet, final String column) {
        try {
            return resultSet.findColumn(column) > -1;
        } catch (final SQLException e) {
            return false;
        }
    }

}
