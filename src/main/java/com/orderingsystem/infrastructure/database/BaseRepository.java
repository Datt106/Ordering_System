package com.orderingsystem.infrastructure.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

/** Lớp cơ sở cho repository JDBC. */
abstract class BaseRepository {

    @FunctionalInterface
    protected interface PreparedStatementSetter {
        void bind(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    protected interface ResultSetHandler<T> {
        T handle(ResultSet resultSet) throws SQLException;
    }

    protected int executeUpdate(Connection connection, String sql, PreparedStatementSetter setter) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (setter != null) {
                setter.bind(statement);
            }
            return statement.executeUpdate();
        } catch (SQLException ex) {
            throw wrapSQLException(ex);
        }
    }

    protected <T> T executeQuery(
            Connection connection,
            String sql,
            PreparedStatementSetter setter,
            ResultSetHandler<T> handler
    ) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (setter != null) {
                setter.bind(statement);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return handler.handle(resultSet);
            }
        } catch (SQLException ex) {
            throw wrapSQLException(ex);
        }
    }

    protected static PreparedStatementSetter bind(Object... parameters) {
        return statement -> {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
        };
    }

    protected <T> T inJdbcTransaction(Function<Connection, T> work) {
        try (Connection connection = DbManager.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);
                T result = work.apply(connection);
                connection.commit();
                return result;
            } catch (RuntimeException | SQLException ex) {
                rollbackQuietly(connection);
                if (ex instanceof RuntimeException runtime) {
                    throw runtime;
                }
                throw wrapSQLException((SQLException) ex);
            } finally {
                restoreAutoCommit(connection, previousAutoCommit);
            }
        } catch (SQLException ex) {
            throw wrapSQLException(ex);
        }
    }

    protected <T> T jdbcQuery(Function<Connection, T> work) {
        try (Connection connection = DbManager.getConnection()) {
            return work.apply(connection);
        } catch (SQLException ex) {
            throw wrapSQLException(ex);
        }
    }

    private static void rollbackQuietly(Connection connection) {
        try {
            if (!connection.isClosed()) {
                connection.rollback();
            }
        } catch (SQLException ignored) {
            // best effort
        }
    }

    private static void restoreAutoCommit(Connection connection, boolean previousAutoCommit)
            throws SQLException {
        if (!connection.isClosed()) {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    protected static IllegalStateException wrapSQLException(SQLException ex) {
        return new IllegalStateException("Lỗi truy cập cơ sở dữ liệu: " + ex.getMessage(), ex);
    }
}
