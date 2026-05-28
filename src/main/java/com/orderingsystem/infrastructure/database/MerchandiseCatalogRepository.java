package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.StandardMerchandise;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MerchandiseCatalogRepository extends BaseRepository {

    public void save(StandardMerchandise item) {
        inJdbcTransaction(connection -> {
            if (existsByCode(item.getMerchandiseCode())) {
                executeUpdate(connection,
                        "UPDATE standard_merchandise SET merchandise_name = ?, description = ? WHERE merchandise_code = ?",
                        bind(item.getMerchandiseName(), item.getDescription(), item.getMerchandiseCode()));
            } else {
                executeUpdate(connection,
                        "INSERT INTO standard_merchandise (merchandise_code, merchandise_name, description) VALUES (?, ?, ?)",
                        bind(item.getMerchandiseCode(), item.getMerchandiseName(), item.getDescription()));
            }
            return null;
        });
    }

    public Optional<StandardMerchandise> findByCode(String merchandiseCode) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT * FROM standard_merchandise WHERE merchandise_code = ?",
                bind(merchandiseCode),
                rs -> rs.next() ? Optional.of(mapMerchandise(rs)) : Optional.empty()));
    }

    public void updateInfo(String merchandiseCode, String merchandiseName, String description) {
        int updated = inJdbcTransaction(connection -> executeUpdate(connection,
                "UPDATE standard_merchandise SET merchandise_name = ?, description = ? WHERE merchandise_code = ?",
                bind(merchandiseName, description, merchandiseCode)));
        if (updated == 0) {
            throw new IllegalArgumentException("Mã hàng không tồn tại trong danh mục chuẩn: " + merchandiseCode);
        }
    }

    public boolean existsByCode(String merchandiseCode) {
        return findByCode(merchandiseCode).isPresent();
    }

    public List<StandardMerchandise> findAllOrderByCode() {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT * FROM standard_merchandise ORDER BY merchandise_code",
                null,
                rs -> {
                    List<StandardMerchandise> items = new ArrayList<>();
                    while (rs.next()) {
                        items.add(mapMerchandise(rs));
                    }
                    return items;
                }));
    }

    public void deleteByCode(String merchandiseCode) {
        inJdbcTransaction(connection -> executeUpdate(connection,
                "DELETE FROM standard_merchandise WHERE merchandise_code = ?",
                bind(merchandiseCode)));
    }

    public boolean isReferenced(String merchandiseCode) {
        return jdbcQuery(connection -> {
            Long inRequests = executeQuery(connection,
                    "SELECT COUNT(*) FROM import_request_items WHERE merchandise_code = ?",
                    bind(merchandiseCode), rs -> {
                        rs.next();
                        return rs.getLong(1);
                    });
            if (inRequests > 0) return true;
            Long atSites = executeQuery(connection,
                    "SELECT COUNT(*) FROM site_merchandise WHERE merchandise_code = ?",
                    bind(merchandiseCode), rs -> {
                        rs.next();
                        return rs.getLong(1);
                    });
            if (atSites > 0) return true;
            Long inOrders = executeQuery(connection,
                    "SELECT COUNT(*) FROM purchase_orders WHERE merchandise_code = ?",
                    bind(merchandiseCode), rs -> {
                        rs.next();
                        return rs.getLong(1);
                    });
            if (inOrders > 0) return true;
            Long inInventory = executeQuery(connection,
                    "SELECT COUNT(*) FROM inventory_queries WHERE merchandise_code = ?",
                    bind(merchandiseCode), rs -> {
                        rs.next();
                        return rs.getLong(1);
                    });
            return inInventory > 0;
        });
    }

    private static StandardMerchandise mapMerchandise(ResultSet rs) throws SQLException {
        return new StandardMerchandise(
                rs.getString("merchandise_code"),
                rs.getString("merchandise_name"),
                rs.getString("description")
        );
    }
}
