package com.harleyoconnor.serdes.util;

import com.harleyoconnor.javautilities.function.ThrowableBiFunction;
import com.harleyoconnor.serdes.exception.NoSuchColumnException;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles converting columns from a {@link ResultSet} to a Java {@code object}.
 *
 * @author Harley O'Connor
 */
public final class ResultSetConversions {

    /**
     * A registry for conversion functions, which handle converting a
     * {@link ResultSet} to a Java {@code object}.
     */
    public static final class ConverterRegistry {
        private final Map<Class<?>, ThrowableBiFunction<ResultSet, String, ?, SQLException>> converters = new HashMap<>();

        /**
         * Constructs a new {@link ConverterRegistry}. Has {@code private} access to
         * prevent initialisation from outside this class.
         */
        private ConverterRegistry() {
        }

        /**
         * Registers a new conversion function. If one for the specified type already
         * exists, it is overridden.
         *
         * @param type The {@link Class} type to register the converter for.
         * @param converter The converter, a {@link ThrowableBiFunction} which takes a
         *                  {@link ResultSet} and a {@link String} and returns an
         *                  {@code object} of type {@link T}. It is also able to throw
         *                  an {@link SQLException}.
         * @param <T> The type to register the converter for.
         * @return This {@link ConverterRegistry} for chaining.
         */
        public <T> ConverterRegistry register(final Class<T> type, final ThrowableBiFunction<ResultSet, String, T, SQLException> converter) {
            this.converters.put(type, converter);
            return this;
        }

        /**
         * Gets an {@link Optional} containing a converter for the specified type,
         * or {@link Optional#empty()} if one didnt exist.
         *
         * @param type The {@link Class} type to get the converter for.
         * @param <T> The type the converter returns.
         * @return The {@link Optional} containing the converter for the specified
         *         type; otherwise {@link Optional#empty()} if one didn't exist.
         */
        @SuppressWarnings("unchecked")
        public <T> Optional<ThrowableBiFunction<ResultSet, String, T, SQLException>> get(final Class<T> type) {
            return this.converters.entrySet().stream()
                    .filter(entrySet -> entrySet.getKey() == type)
                    .map(Map.Entry::getValue)
                    .findAny()
                    .map(function -> ((ThrowableBiFunction<ResultSet, String, T, SQLException>) function));
        }
    }

    public static final ConverterRegistry CONVERTER_REGISTRY = new ConverterRegistry()
            .register(Boolean.class, ResultSet::getBoolean)
            .register(String.class, ResultSet::getString)
            .register(Integer.class, ResultSet::getInt)
            .register(Double.class, ResultSet::getDouble)
            .register(Float.class, ResultSet::getFloat)
            .register(BigDecimal.class, ResultSet::getBigDecimal)
            .register(Date.class, ResultSet::getDate)
            .register(Time.class, ResultSet::getTime)
            .register(Timestamp.class, ResultSet::getTimestamp)
            .register(java.util.Date.class, ResultSet::getDate);

    /**
     * Attempts to get the value of the specified {@code valueType} from the
     * specified {@code column}. This method is an "unchecked" version of
     * {@link #getValue(ResultSet, String, Class)} as it throws any
     * {@link SQLException}s as a {@link RuntimeException}.
     *
     * @param resultSet The {@link ResultSet} to fetch the value from.
     * @param column The {@code column} to fetch the value from.
     * @param valueType The {@link Class} of the value to fetch.
     * @param <V> The type of the value to fetch.
     * @return The value; otherwise {@code null} if it didn't exist in the column.
     * @throws RuntimeException If there were any {@link SQLException} thrown
     *                          by {@link #getValue(ResultSet, String, Class)}.
     */
    @Nullable
    public static <V> V getValueUnchecked(final ResultSet resultSet, final String column, final Class<V> valueType) {
        try {
            return getValue(resultSet, column, valueType);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Attempts to get the value of the specified {@code valueType} from the
     * specified {@code column}.
     *
     * @param resultSet The {@link ResultSet} to fetch the value from.
     * @param column The {@code column} to fetch the value from.
     * @param valueType The {@link Class} of the value to fetch.
     * @param <V> The type of the value to fetch.
     * @return The value; otherwise {@code null} if it didn't exist in the column.
     * @throws SQLException If the specified {@code column} did not exist in the
     *                      specified {@link ResultSet}; if a database access
     *                      exception occurs; if the specified {@link ResultSet}
     *                      is closed.
     * @throws IllegalArgumentException If there was no registered conversion
     *                                  function for the given value {@link Class}.
     */
    @Nullable
    public static <V> V getValue(final ResultSet resultSet, final String column, final Class<V> valueType) throws SQLException {
        if (!SQLHelper.containsColumn(resultSet, column))
            throw new NoSuchColumnException("No such column '" + column + "'.");

        return CONVERTER_REGISTRY.get(valueType)
                .orElseThrow(() -> new IllegalArgumentException("Could not get value of type '" + valueType + "'."))
                .apply(resultSet, column);
    }

}
