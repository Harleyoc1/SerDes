package com.harleyoconnor.serdes.database;

import com.harleyoconnor.serdes.exception.NoSuchRowException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains various methods for interacting with a {@link Database}. Currently
 * highly work in progress and so doesn't have much Javadoc.
 *
 * @author Harley O'Connor
 */
public class Database {

    private final Connection connection;

    public Database(Connection connection) {
        this.connection = connection;
    }

    /**
     * Gets the {@link Connection} that this {@link Database} controls.
     *
     * @return The {@link Connection} for the {@code database}.
     */
    public Connection getConnection() {
        return this.connection;
    }

    public ResultSet select(String table, String valueName, Object value) throws SQLException {
        final var statement = this.connection.prepareStatement("select * from `" + table + "` where " + valueName + " = ?",
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

        statement.setObject(1, value);

        final var resultSet = statement.executeQuery();

        if (!resultSet.next())
            throw new NoSuchRowException("No row could be found where '" + valueName + "' is '" + value + "'.");

        return resultSet;
    }

    public ResultSet selectUnchecked(String table, String valueName, Object value) {
        try {
            return this.select(table, valueName, value);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(final String table, final String primaryFieldName, final Object primaryFieldValue, final LinkedHashMap<String, Object> valuesToUpdate) throws SQLException {
        final var statementBuilder = new StringBuilder("update `" + table + "` set ");
        final var fieldNames = new ArrayList<>(valuesToUpdate.keySet());

        for (int i = 0; i < fieldNames.size(); i++) {
            statementBuilder.append(fieldNames.get(i)).append(" = ?").append(i != fieldNames.size() - 1 ? ", " : " ");
        }

        statementBuilder.append("where ").append(primaryFieldName).append( " = ?;");

        final var args = fieldNames.stream().map(valuesToUpdate::get).collect(Collectors.toList());
        args.add(primaryFieldValue);

        this.executePreparedStatement(statementBuilder.toString(), args);
    }

    public void updateUnchecked(final String table, final String primaryFieldName, final Object primaryFieldValue, final LinkedHashMap<String, Object> valuesToUpdate) {
        try {
            this.update(table, primaryFieldName, primaryFieldValue, valuesToUpdate);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insert(final String table, final LinkedHashMap<String, Object> valuesToInsert) throws SQLException {
        final var statementBuilder = new StringBuilder("insert into `" + table + "` (");
        final var fieldNames = new ArrayList<>(valuesToInsert.keySet());

        for (int i = 0; i < fieldNames.size(); i++) {
            statementBuilder.append(fieldNames.get(i)).append(i != fieldNames.size() - 1 ? ", " : " ");
        }

        statementBuilder.append(") values (");

        for (int i = 0; i < fieldNames.size(); i++) {
            statementBuilder.append("?").append(i != fieldNames.size() - 1 ? ", " : " ");
        }

        this.executePreparedStatement(statementBuilder.append(")").toString(), fieldNames.stream().map(valuesToInsert::get).collect(Collectors.toList()));
    }

    public void insertUnchecked(final String table, final LinkedHashMap<String, Object> valuesToInsert) {
        try {
            this.insert(table, valuesToInsert);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getMaxOrDefault(final String table, final String fieldName, final int defaultValue) {
        try {
            final int max = this.getMax(table, fieldName);
            return max == -1 ? defaultValue : max;
        } catch (final SQLException e) {
            return defaultValue;
        }
    }

    public int getMaxUnchecked(final String table, final String fieldName) {
        try {
            return this.getMax(table, fieldName);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getMax(final String table, final String fieldName) throws SQLException {
        final var resultSet = this.connection.prepareStatement("select max(" + fieldName + ") from " + table).executeQuery();

        if (!resultSet.next())
            return -1;

        return resultSet.getInt(1);
    }

    public boolean valueExists(final String table, final String fieldName, final Object fieldValue) {
        try {
            // TODO: Make a method that doesn't involve pointlessly transferring this data to the client.
            this.select(table, fieldName, fieldValue);
        } catch (final NoSuchRowException e) {
            return false;
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public void executePreparedStatement(final String sqlQuery, final List<Object> args) throws SQLException {
        final var statement = this.connection.prepareStatement(sqlQuery);

        for (int i = 1; i <= args.size(); i++) {
            statement.setObject(i, args.get(i - 1));
        }

        statement.executeQuery();
    }

}