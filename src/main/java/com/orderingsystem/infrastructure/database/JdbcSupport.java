package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.DeliveryMeans;
import com.orderingsystem.core.domain.ItemStatus;
import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.core.domain.ShippingStatus;
import com.orderingsystem.core.domain.UserRole;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;

/** Chuyển đổi kiểu Java ↔ SQLite cho repository JDBC. */
final class JdbcSupport {

    private JdbcSupport() {
    }

    static Instant getInstant(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : Instant.parse(value);
    }

    static LocalDate getLocalDate(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : LocalDate.parse(value);
    }

    static boolean getBoolean(ResultSet rs, String column) throws SQLException {
        return rs.getInt(column) != 0;
    }

    static void setInstant(PreparedStatement ps, int index, Instant value) throws SQLException {
        if (value == null) {
            ps.setObject(index, null);
        } else {
            ps.setString(index, value.toString());
        }
    }

    static void setLocalDate(PreparedStatement ps, int index, LocalDate value) throws SQLException {
        if (value == null) {
            ps.setObject(index, null);
        } else {
            ps.setString(index, value.toString());
        }
    }

    static void setBoolean(PreparedStatement ps, int index, boolean value) throws SQLException {
        ps.setInt(index, value ? 1 : 0);
    }

    static UserRole getUserRole(ResultSet rs, String column) throws SQLException {
        return UserRole.valueOf(rs.getString(column));
    }

    static RequestStatus getRequestStatus(ResultSet rs, String column) throws SQLException {
        return RequestStatus.valueOf(rs.getString(column));
    }

    static ItemStatus getItemStatus(ResultSet rs, String column) throws SQLException {
        return ItemStatus.valueOf(rs.getString(column));
    }

    static ShippingStatus getShippingStatus(ResultSet rs, String column) throws SQLException {
        return ShippingStatus.valueOf(rs.getString(column));
    }

    static OrderStatus getOrderStatus(ResultSet rs, String column) throws SQLException {
        return OrderStatus.valueOf(rs.getString(column));
    }

    static DeliveryMeans getDeliveryMeans(ResultSet rs, String column) throws SQLException {
        return DeliveryMeans.valueOf(rs.getString(column));
    }
}
