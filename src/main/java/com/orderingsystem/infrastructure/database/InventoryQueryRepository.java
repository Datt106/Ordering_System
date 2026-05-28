package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.InventoryQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryQueryRepository extends BaseRepository {

    public Optional<InventoryQuery> findById(String queryId) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT * FROM inventory_queries WHERE query_id = ?",
                bind(queryId),
                rs -> rs.next() ? Optional.of(mapQuery(rs)) : Optional.empty()));
    }

    public void save(InventoryQuery query) {
        inJdbcTransaction(connection -> {
            if (findById(query.getQueryId()).isPresent()) {
                executeUpdate(connection,
                        "UPDATE inventory_queries SET request_id = ?, site_code = ?, merchandise_code = ?, in_stock_quantity = ?, unit = ?, responded_at = ? WHERE query_id = ?",
                        statement -> {
                            statement.setString(1, query.getRequestId());
                            statement.setString(2, query.getSiteCode());
                            statement.setString(3, query.getMerchandiseCode());
                            statement.setInt(4, query.getInStockQuantity());
                            statement.setString(5, query.getUnit());
                            JdbcSupport.setInstant(statement, 6, query.getRespondedAt());
                            statement.setString(7, query.getQueryId());
                        });
            } else {
                executeUpdate(connection,
                        "INSERT INTO inventory_queries (query_id, request_id, site_code, merchandise_code, in_stock_quantity, unit, responded_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        statement -> {
                            statement.setString(1, query.getQueryId());
                            statement.setString(2, query.getRequestId());
                            statement.setString(3, query.getSiteCode());
                            statement.setString(4, query.getMerchandiseCode());
                            statement.setInt(5, query.getInStockQuantity());
                            statement.setString(6, query.getUnit());
                            JdbcSupport.setInstant(statement, 7, query.getRespondedAt());
                        });
            }
            return null;
        });
    }

    public void saveAll(List<InventoryQuery> queries) {
        inJdbcTransaction(connection -> {
            for (InventoryQuery q : queries) {
                executeUpdate(connection,
                        "INSERT OR REPLACE INTO inventory_queries (query_id, request_id, site_code, merchandise_code, in_stock_quantity, unit, responded_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        statement -> {
                            statement.setString(1, q.getQueryId());
                            statement.setString(2, q.getRequestId());
                            statement.setString(3, q.getSiteCode());
                            statement.setString(4, q.getMerchandiseCode());
                            statement.setInt(5, q.getInStockQuantity());
                            statement.setString(6, q.getUnit());
                            JdbcSupport.setInstant(statement, 7, q.getRespondedAt());
                        });
            }
            return null;
        });
    }

    public List<InventoryQuery> findByRequestId(String requestId) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT * FROM inventory_queries WHERE request_id = ? ORDER BY site_code, merchandise_code",
                bind(requestId),
                rs -> {
                    List<InventoryQuery> list = new ArrayList<>();
                    while (rs.next()) list.add(mapQuery(rs));
                    return list;
                }));
    }

    public void deleteByRequestId(String requestId) {
        inJdbcTransaction(connection -> executeUpdate(connection,
                "DELETE FROM inventory_queries WHERE request_id = ?",
                bind(requestId)));
    }

    public List<InventoryQuery> findPendingBySiteCode(String siteCode) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT * FROM inventory_queries WHERE site_code = ? AND responded_at IS NULL ORDER BY request_id, merchandise_code",
                bind(siteCode),
                rs -> {
                    List<InventoryQuery> list = new ArrayList<>();
                    while (rs.next()) list.add(mapQuery(rs));
                    return list;
                }));
    }

    public long countByRequestId(String requestId) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT COUNT(*) FROM inventory_queries WHERE request_id = ?",
                bind(requestId),
                rs -> {
                    rs.next();
                    return rs.getLong(1);
                }));
    }

    public long countRespondedByRequestId(String requestId) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT COUNT(*) FROM inventory_queries WHERE request_id = ? AND responded_at IS NOT NULL",
                bind(requestId),
                rs -> {
                    rs.next();
                    return rs.getLong(1);
                }));
    }

    private static InventoryQuery mapQuery(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new InventoryQuery(
                rs.getString("query_id"),
                rs.getString("request_id"),
                rs.getString("site_code"),
                rs.getString("merchandise_code"),
                rs.getInt("in_stock_quantity"),
                rs.getString("unit"),
                JdbcSupport.getInstant(rs, "responded_at")
        );
    }
}
