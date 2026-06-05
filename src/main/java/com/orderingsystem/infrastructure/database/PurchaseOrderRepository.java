package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.core.domain.PurchaseOrder;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PurchaseOrderRepository extends BaseRepository {

    private static final Set<OrderStatus> ACTIVE_ORDER_STATUSES = EnumSet.of(
            OrderStatus.CHO_GUI,
            OrderStatus.DA_GUI,
            OrderStatus.DA_XAC_NHAN,
            OrderStatus.SAI_LECH
    );

    // Câu lệnh gốc được bổ sung JOIN với bảng sites và standard_merchandise để lấy Tên
    private static final String SELECT_BASE = 
            "SELECT po.*, s.site_name, sm.merchandise_name " +
            "FROM purchase_orders po " +
            "LEFT JOIN sites s ON po.site_code = s.site_code " +
            "LEFT JOIN standard_merchandise sm ON po.merchandise_code = sm.merchandise_code ";

    public void save(PurchaseOrder order) {
        inJdbcTransaction(connection -> executeUpdate(connection,
                "INSERT OR REPLACE INTO purchase_orders (order_id, request_id, site_code, merchandise_code, quantity_ordered, unit, delivery_means, status, sent_at, confirmed_at, actual_quantity, quantity_diff, reconciled_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                statement -> {
                    statement.setString(1, order.getOrderId());
                    statement.setString(2, order.getRequestId());
                    statement.setString(3, order.getSiteCode());
                    statement.setString(4, order.getMerchandiseCode());
                    statement.setInt(5, order.getQuantityOrdered());
                    statement.setString(6, order.getUnit());
                    statement.setString(7, order.getDeliveryMeans().name());
                    statement.setString(8, order.getStatus().name());
                    JdbcSupport.setInstant(statement, 9, order.getSentAt());
                    JdbcSupport.setInstant(statement, 10, order.getConfirmedAt());
                    statement.setObject(11, order.getActualQuantity());
                    statement.setObject(12, order.getQuantityDiff());
                    JdbcSupport.setInstant(statement, 13, order.getReconciledAt());
                }));
    }

    public void saveAll(List<PurchaseOrder> orders) {
        inJdbcTransaction(connection -> {
            for (PurchaseOrder order : orders) {
                executeUpdate(connection,
                        "INSERT OR REPLACE INTO purchase_orders (order_id, request_id, site_code, merchandise_code, quantity_ordered, unit, delivery_means, status, sent_at, confirmed_at, actual_quantity, quantity_diff, reconciled_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        statement -> {
                            statement.setString(1, order.getOrderId());
                            statement.setString(2, order.getRequestId());
                            statement.setString(3, order.getSiteCode());
                            statement.setString(4, order.getMerchandiseCode());
                            statement.setInt(5, order.getQuantityOrdered());
                            statement.setString(6, order.getUnit());
                            statement.setString(7, order.getDeliveryMeans().name());
                            statement.setString(8, order.getStatus().name());
                            JdbcSupport.setInstant(statement, 9, order.getSentAt());
                            JdbcSupport.setInstant(statement, 10, order.getConfirmedAt());
                            statement.setObject(11, order.getActualQuantity());
                            statement.setObject(12, order.getQuantityDiff());
                            JdbcSupport.setInstant(statement, 13, order.getReconciledAt());
                        });
            }
            return null;
        });
    }

    public Optional<PurchaseOrder> findById(String orderId) {
        return jdbcQuery(connection -> executeQuery(connection,
                SELECT_BASE + "WHERE po.order_id = ?",
                bind(orderId),
                rs -> rs.next() ? Optional.of(mapOrder(rs)) : Optional.empty()));
    }

    public List<String> findDistinctRequestIdsByStatus(OrderStatus status) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT DISTINCT request_id FROM purchase_orders WHERE status = ? ORDER BY request_id",
                bind(status.name()),
                rs -> {
                    List<String> ids = new ArrayList<>();
                    while (rs.next()) {
                        ids.add(rs.getString("request_id"));
                    }
                    return ids;
                }));
    }

    public List<PurchaseOrder> findByRequestId(String requestId) {
        return jdbcQuery(connection -> executeQuery(connection,
                SELECT_BASE + "WHERE po.request_id = ? ORDER BY po.site_code, po.merchandise_code",
                bind(requestId),
                rs -> {
                    List<PurchaseOrder> list = new ArrayList<>();
                    while (rs.next()) list.add(mapOrder(rs));
                    return list;
                }));
    }

    public void deleteByRequestId(String requestId) {
        inJdbcTransaction(connection -> executeUpdate(connection,
                "DELETE FROM purchase_orders WHERE request_id = ?",
                bind(requestId)));
    }

    public List<PurchaseOrder> findByStatus(OrderStatus status) {
        return jdbcQuery(connection -> executeQuery(connection,
                SELECT_BASE + "WHERE po.status = ? ORDER BY po.sent_at DESC",
                bind(status.name()),
                rs -> {
                    List<PurchaseOrder> list = new ArrayList<>();
                    while (rs.next()) list.add(mapOrder(rs));
                    return list;
                }));
    }

    public List<PurchaseOrder> findBySiteCodeAndStatuses(String siteCode, Set<OrderStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        String placeholders = statuses.stream().map(s -> "?").collect(Collectors.joining(","));
        String sql = SELECT_BASE + "WHERE po.site_code = ? AND po.status IN (" + placeholders + ") ORDER BY po.request_id, po.merchandise_code";
        Object[] params = new Object[statuses.size() + 1];
        params[0] = siteCode;
        int idx = 1;
        for (OrderStatus s : statuses) {
            params[idx++] = s.name();
        }
        return jdbcQuery(connection -> executeQuery(connection,
                sql,
                bind(params),
                rs -> {
                    List<PurchaseOrder> list = new ArrayList<>();
                    while (rs.next()) list.add(mapOrder(rs));
                    return list;
                }));
    }

    public List<PurchaseOrder> findAll() {
        return jdbcQuery(connection -> executeQuery(connection,
                SELECT_BASE + "ORDER BY po.request_id, po.site_code, po.merchandise_code",
                null,
                rs -> {
                    List<PurchaseOrder> list = new ArrayList<>();
                    while (rs.next()) list.add(mapOrder(rs));
                    return list;
                }));
    }

    public List<PurchaseOrder> findByStatuses(Set<OrderStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        String placeholders = statuses.stream().map(s -> "?").collect(Collectors.joining(","));
        String sql = SELECT_BASE + "WHERE po.status IN (" + placeholders + ") ORDER BY po.request_id, po.site_code";
        Object[] params = statuses.stream().map(Enum::name).toArray();
        return jdbcQuery(connection -> executeQuery(connection,
                sql,
                bind(params),
                rs -> {
                    List<PurchaseOrder> list = new ArrayList<>();
                    while (rs.next()) list.add(mapOrder(rs));
                    return list;
                }));
    }

    public boolean hasActiveOrdersForSite(String siteCode) {
        String placeholders = ACTIVE_ORDER_STATUSES.stream().map(s -> "?").collect(Collectors.joining(","));
        String sql = "SELECT COUNT(*) FROM purchase_orders WHERE site_code = ? AND status IN (" + placeholders + ")";
        Object[] params = new Object[ACTIVE_ORDER_STATUSES.size() + 1];
        params[0] = siteCode;
        int idx = 1;
        for (OrderStatus s : ACTIVE_ORDER_STATUSES) {
            params[idx++] = s.name();
        }
        return jdbcQuery(connection -> executeQuery(connection, sql, bind(params), rs -> {
            rs.next();
            return rs.getLong(1) > 0;
        }));
    }

    private static PurchaseOrder mapOrder(java.sql.ResultSet rs) throws java.sql.SQLException {
        PurchaseOrder order = new PurchaseOrder(
                rs.getString("order_id"),
                rs.getString("request_id"),
                rs.getString("site_code"),
                rs.getString("merchandise_code"),
                rs.getInt("quantity_ordered"),
                rs.getString("unit"),
                JdbcSupport.getDeliveryMeans(rs, "delivery_means")
        );
        order.setStatus(JdbcSupport.getOrderStatus(rs, "status"));
        order.setSentAt(JdbcSupport.getInstant(rs, "sent_at"));
        order.setConfirmedAt(JdbcSupport.getInstant(rs, "confirmed_at"));
        order.setActualQuantity((Integer) rs.getObject("actual_quantity"));
        order.setQuantityDiff((Integer) rs.getObject("quantity_diff"));
        
        // Thêm mapping cho các dữ liệu mới
        order.setReconciledAt(JdbcSupport.getInstant(rs, "reconciled_at"));
        order.setSiteName(rs.getString("site_name"));
        order.setMerchandiseName(rs.getString("merchandise_name"));
        
        return order;
    }
}