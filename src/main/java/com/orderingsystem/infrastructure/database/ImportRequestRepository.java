package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.ImportRequestItem;
import com.orderingsystem.core.domain.ItemStatus;
import com.orderingsystem.core.domain.RequestStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImportRequestRepository extends BaseRepository {

    public void save(ImportRequest request) {
        inJdbcTransaction(connection -> {
            boolean existed = executeQuery(connection,
                    "SELECT 1 FROM import_requests WHERE request_id = ?",
                    bind(request.getRequestId()),
                    rs -> rs.next());

            if (existed) {
                executeUpdate(connection,
                        "UPDATE import_requests SET created_at = ?, created_by = ?, department = ?, status = ?, processed_by = ?, processed_at = ? WHERE request_id = ?",
                        statement -> {
                            JdbcSupport.setInstant(statement, 1, request.getCreatedAt());
                            statement.setString(2, request.getCreatedBy());
                            statement.setString(3, request.getDepartment());
                            statement.setString(4, request.getStatus().name());
                            statement.setString(5, request.getProcessedBy());
                            JdbcSupport.setInstant(statement, 6, request.getProcessedAt());
                            statement.setString(7, request.getRequestId());
                        });
                executeUpdate(connection,
                        "DELETE FROM import_request_items WHERE request_id = ?",
                        bind(request.getRequestId()));
            } else {
                executeUpdate(connection,
                        "INSERT INTO import_requests (request_id, created_at, created_by, department, status, processed_by, processed_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        statement -> {
                            statement.setString(1, request.getRequestId());
                            JdbcSupport.setInstant(statement, 2, request.getCreatedAt());
                            statement.setString(3, request.getCreatedBy());
                            statement.setString(4, request.getDepartment());
                            statement.setString(5, request.getStatus().name());
                            statement.setString(6, request.getProcessedBy());
                            JdbcSupport.setInstant(statement, 7, request.getProcessedAt());
                        });
            }

            for (ImportRequestItem item : request.getItems()) {
                executeUpdate(connection,
                        "INSERT INTO import_request_items (request_id, merchandise_code, quantity_ordered, unit, desired_delivery_date, item_status) VALUES (?, ?, ?, ?, ?, ?)",
                        statement -> {
                            statement.setString(1, request.getRequestId());
                            statement.setString(2, item.getMerchandiseCode());
                            statement.setInt(3, item.getQuantityOrdered());
                            statement.setString(4, item.getUnit());
                            JdbcSupport.setLocalDate(statement, 5, item.getDesiredDeliveryDate());
                            statement.setString(6, item.getItemStatus().name());
                        });
            }
            return null;
        });
    }

    public Optional<ImportRequest> findById(String requestId) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT * FROM import_requests WHERE request_id = ?",
                bind(requestId),
                rs -> rs.next() ? Optional.of(mapRequest(rs)) : Optional.empty()));
    }

    public Optional<ImportRequest> findByIdWithItems(String requestId) {
        return jdbcQuery(connection -> {
            Optional<ImportRequest> request = executeQuery(connection,
                    "SELECT * FROM import_requests WHERE request_id = ?",
                    bind(requestId),
                    rs -> rs.next() ? Optional.of(mapRequest(rs)) : Optional.empty());
            if (request.isEmpty()) {
                return Optional.empty();
            }
            ImportRequest loaded = request.get();
            List<ImportRequestItem> items = executeQuery(connection,
                    "SELECT * FROM import_request_items WHERE request_id = ? ORDER BY id",
                    bind(requestId),
                    rs -> {
                        List<ImportRequestItem> list = new ArrayList<>();
                        while (rs.next()) {
                            list.add(mapItem(rs));
                        }
                        return list;
                    });
            for (ImportRequestItem item : items) {
                loaded.addItem(item);
            }
            return Optional.of(loaded);
        });
    }

    public int countByRequestIdPrefix(String prefix) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT COUNT(*) FROM import_requests WHERE request_id LIKE ?",
                bind(prefix + "%"),
                rs -> {
                    rs.next();
                    return rs.getInt(1);
                }));
    }

    public List<ImportRequest> findByDepartment(String department) {
        return findByDepartmentFiltered(department, null, null, null);
    }

    public List<ImportRequest> findByDepartmentFiltered(
            String department,
            RequestStatus status,
            Instant createdFromInclusive,
            Instant createdToExclusive
    ) {
        StringBuilder sql = new StringBuilder("SELECT * FROM import_requests WHERE department = ?");
        List<Object> params = new ArrayList<>();
        params.add(department);
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status.name());
        }
        if (createdFromInclusive != null) {
            sql.append(" AND created_at >= ?");
            params.add(createdFromInclusive.toString());
        }
        if (createdToExclusive != null) {
            sql.append(" AND created_at < ?");
            params.add(createdToExclusive.toString());
        }
        sql.append(" ORDER BY created_at DESC");

        return jdbcQuery(connection -> executeQuery(connection,
                sql.toString(),
                bind(params.toArray()),
                rs -> {
                    List<ImportRequest> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRequest(rs));
                    }
                    return list;
                }));
    }

    public long countItemsByRequestId(String requestId) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT COUNT(*) FROM import_request_items WHERE request_id = ?",
                bind(requestId),
                rs -> {
                    rs.next();
                    return rs.getLong(1);
                }));
    }

    public List<ImportRequest> findByStatus(RequestStatus status) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT * FROM import_requests WHERE status = ? ORDER BY created_at DESC",
                bind(status.name()),
                rs -> {
                    List<ImportRequest> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRequest(rs));
                    }
                    return list;
                }));
    }

    public void updateStatus(String requestId, RequestStatus status, String processedBy) {
        int updated = inJdbcTransaction(connection -> executeUpdate(connection,
                "UPDATE import_requests SET status = ?, processed_by = ?, processed_at = ? WHERE request_id = ?",
                statement -> {
                    statement.setString(1, status.name());
                    statement.setString(2, processedBy);
                    JdbcSupport.setInstant(statement, 3, processedBy != null ? Instant.now() : null);
                    statement.setString(4, requestId);
                }));
        if (updated == 0) {
            throw new IllegalArgumentException("Yêu cầu không tồn tại: " + requestId);
        }
    }

    public void updateItemStatus(long itemId, ItemStatus status) {
        int updated = inJdbcTransaction(connection -> executeUpdate(connection,
                "UPDATE import_request_items SET item_status = ? WHERE id = ?",
                bind(status.name(), itemId)));
        if (updated == 0) {
            throw new IllegalArgumentException("Dòng yêu cầu không tồn tại: " + itemId);
        }
    }

    public void acceptForProcessing(String requestId, String processedBy) {
        requirePendingStatus(requestId, "Chỉ tiếp nhận được yêu cầu ở trạng thái Chờ xử lý.");
        updateStatusWithProcessor(requestId, RequestStatus.DANG_XU_LY, processedBy);
    }

    /** Overseas từ chối yêu cầu — chỉ khi còn Chờ xử lý. */
    public void rejectRequest(String requestId, String processedBy) {
        requirePendingStatus(requestId, "Chỉ từ chối được yêu cầu ở trạng thái Chờ xử lý.");
        updateStatusWithProcessor(requestId, RequestStatus.TU_CHOI, processedBy);
    }

    private void requirePendingStatus(String requestId, String messagePrefix) {
        ImportRequest request = findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Yêu cầu không tồn tại: " + requestId));
        if (request.getStatus() != RequestStatus.CHO_XU_LY) {
            throw new IllegalStateException(
                    messagePrefix + " Hiện tại: " + request.getStatus());
        }
    }

    private void updateStatusWithProcessor(String requestId, RequestStatus status, String processedBy) {
        inJdbcTransaction(connection -> executeUpdate(connection,
                "UPDATE import_requests SET status = ?, processed_by = ?, processed_at = ? WHERE request_id = ?",
                statement -> {
                    statement.setString(1, status.name());
                    statement.setString(2, processedBy);
                    JdbcSupport.setInstant(statement, 3, Instant.now());
                    statement.setString(4, requestId);
                }));
    }

    private static ImportRequest mapRequest(java.sql.ResultSet rs) throws java.sql.SQLException {
        ImportRequest request = new ImportRequest(
                rs.getString("request_id"),
                rs.getString("created_by"),
                rs.getString("department")
        );
        request.setCreatedAt(JdbcSupport.getInstant(rs, "created_at"));
        request.setStatus(JdbcSupport.getRequestStatus(rs, "status"));
        request.setProcessedBy(rs.getString("processed_by"));
        request.setProcessedAt(JdbcSupport.getInstant(rs, "processed_at"));
        return request;
    }

    private static ImportRequestItem mapItem(java.sql.ResultSet rs) throws java.sql.SQLException {
        ImportRequestItem item = new ImportRequestItem(
                rs.getString("merchandise_code"),
                rs.getInt("quantity_ordered"),
                rs.getString("unit"),
                JdbcSupport.getLocalDate(rs, "desired_delivery_date")
        );
        item.setId(rs.getLong("id"));
        item.setItemStatus(JdbcSupport.getItemStatus(rs, "item_status"));
        return item;
    }
}
