package com.harleyoconnor.serdes.util;

import com.harleyoconnor.javautilities.function.ThrowableBiFunction;
import com.harleyoconnor.serdes.exception.NoSuchColumnException;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class ResultSetConversions {

    public static final class ConverterRegistry {
        private final Map<Class<?>, ThrowableBiFunction<ResultSet, String, ?, SQLException>> CONVERTERS = new HashMap<>();

        public <T> ConverterRegistry register(final Class<T> type, final ThrowableBiFunction<ResultSet, String, T, SQLException> converter) {
            CONVERTERS.put(type, converter);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T> ThrowableBiFunction<ResultSet, String, T, SQLException> get(final Class<T> type) {
            final ThrowableBiFunction<ResultSet, String, ?, SQLException> converter = CONVERTERS.get(type);
            return converter != null ? ((ThrowableBiFunction<ResultSet, String, T, SQLException>) converter) : null;
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

    @Nullable
    public static <V> V getValueUnsafe(final ResultSet resultSet, final String columnName, final Class<V> valueType) {
        try {
            return getValue(resultSet, columnName, valueType);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static <V> V getValue(final ResultSet resultSet, final String columnName, final Class<V> valueType) throws SQLException {
        if (!SQLHelper.containsColumn(resultSet, columnName))
            throw new NoSuchColumnException("No such column '" + columnName + "'.");

        final ThrowableBiFunction<ResultSet, String, V, SQLException> converter = CONVERTER_REGISTRY.get(valueType);

        if (converter == null)
            throw new UnrecognisedObjectException("Could not get value from type '" + valueType + "'.");

        return converter.apply(resultSet, columnName);
    }

    public static final class UnrecognisedObjectException extends RuntimeException {
        public UnrecognisedObjectException(String message) {
            super(message);
        }
    }


}
