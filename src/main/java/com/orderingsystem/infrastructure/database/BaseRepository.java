package com.orderingsystem.infrastructure.database;

import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.jpa.JpaBootstrap;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Lớp cơ sở cho repository — JDBC (Giai đoạn 2 ITSS).
 * <p>
 * <strong>JDBC:</strong> {@link #executeUpdate}, {@link #executeQuery},
 * {@link #inJdbcTransaction(Consumer)} / {@link #inJdbcTransaction(Function)} với {@link Connection},
 * {@link #jdbcQuery(Function)} chỉ đọc.
 * <p>
 * <strong>JPA (tạm):</strong> overload cùng tên với {@link EntityManager} giữ để các
 * {@code *Repository} chưa migrate vẫn biên dịch; sẽ xóa cùng Hibernate ở bước sau.
 */
abstract class BaseRepository {

    @FunctionalInterface
    protected interface PreparedStatementSetter {
        void bind(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    protected interface ResultSetHandler<T> {
        T handle(ResultSet resultSet) throws SQLException;
    }

    // -------------------------------------------------------------------------
    // JDBC — helpers
    // -------------------------------------------------------------------------

    /**
     * Thực thi INSERT/UPDATE/DELETE trên connection đã có (thường trong transaction).
     *
     * @return số dòng bị ảnh hưởng
     */
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

    /**
     * Thực thi SELECT; {@link ResultSet} được đóng sau khi handler kết thúc.
     */
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

  /**
     * Gán tham số theo thứ tự 1..n (tiện cho câu SQL đơn giản).
     */
    protected static PreparedStatementSetter bind(Object... parameters) {
        return statement -> {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
        };
    }

    // -------------------------------------------------------------------------
    // JDBC — transaction & query entry points
    // -------------------------------------------------------------------------

    protected void inJdbcTransaction(Consumer<Connection> work) {
        try (Connection connection = DbManager.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);
                work.accept(connection);
                connection.commit();
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

    // -------------------------------------------------------------------------
    // JPA — legacy (xóa khi migrate xong *Repository)
    // -------------------------------------------------------------------------

    protected void inTransaction(Consumer<EntityManager> work) {
        EntityManager em = JpaBootstrap.openEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            work.accept(em);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    protected <T> T inTransaction(Function<EntityManager, T> work) {
        EntityManager em = JpaBootstrap.openEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    protected <T> T query(Function<EntityManager, T> work) {
        EntityManager em = JpaBootstrap.openEntityManager();
        try {
            return work.apply(em);
        } finally {
            em.close();
        }
    }
}
