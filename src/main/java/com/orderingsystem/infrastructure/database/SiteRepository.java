package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.ShippingStatus;
import com.orderingsystem.core.domain.Site;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SiteRepository extends BaseRepository {

    public void save(Site site) {
        inJdbcTransaction(connection -> {
            if (existsByCode(site.getSiteCode())) {
                executeUpdate(connection,
                        "UPDATE sites SET site_name = ?, ship_days = ?, air_days = ?, other_info = ?, active = ?, shipping_status = ?, shipping_updated_at = ? WHERE site_code = ?",
                        statement -> {
                            statement.setString(1, site.getSiteName());
                            statement.setObject(2, site.getShipDays());
                            statement.setObject(3, site.getAirDays());
                            statement.setString(4, site.getOtherInfo());
                            JdbcSupport.setBoolean(statement, 5, site.isActive());
                            statement.setString(6, site.getShippingStatus().name());
                            JdbcSupport.setInstant(statement, 7, site.getShippingUpdatedAt());
                            statement.setString(8, site.getSiteCode());
                        });
            } else {
                executeUpdate(connection,
                        "INSERT INTO sites (site_code, site_name, ship_days, air_days, other_info, active, shipping_status, shipping_updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        statement -> {
                            statement.setString(1, site.getSiteCode());
                            statement.setString(2, site.getSiteName());
                            statement.setObject(3, site.getShipDays());
                            statement.setObject(4, site.getAirDays());
                            statement.setString(5, site.getOtherInfo());
                            JdbcSupport.setBoolean(statement, 6, site.isActive());
                            statement.setString(7, site.getShippingStatus().name());
                            JdbcSupport.setInstant(statement, 8, site.getShippingUpdatedAt());
                        });
            }
            return null;
        });
    }

    public Optional<Site> findByCode(String siteCode) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT * FROM sites WHERE site_code = ?",
                bind(siteCode),
                rs -> rs.next() ? Optional.of(mapSite(rs)) : Optional.empty()));
    }

    public List<Site> findAll() {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT * FROM sites ORDER BY site_code",
                null,
                rs -> {
                    List<Site> sites = new ArrayList<>();
                    while (rs.next()) sites.add(mapSite(rs));
                    return sites;
                }));
    }

    public List<Site> findAllActive() {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT * FROM sites WHERE active = 1 ORDER BY site_code",
                null,
                rs -> {
                    List<Site> sites = new ArrayList<>();
                    while (rs.next()) sites.add(mapSite(rs));
                    return sites;
                }));
    }

    /**
     * @param keyword    từ khóa (mã, tên, ghi chú); null/blank = không lọc theo chữ
     * @param activeOnly true = hoạt động, false = ngừng, null = tất cả
     */
    public List<Site> search(String keyword, Boolean activeOnly) {
        StringBuilder sql = new StringBuilder("SELECT * FROM sites WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(site_code) LIKE ? OR LOWER(site_name) LIKE ? OR LOWER(COALESCE(other_info, '')) LIKE ?)");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (activeOnly != null) {
            sql.append(" AND active = ?");
            params.add(activeOnly ? 1 : 0);
        }
        sql.append(" ORDER BY site_code");
        Object[] bindParams = params.toArray();
        return jdbcQuery(connection -> executeQuery(connection,
                sql.toString(),
                bindParams.length == 0 ? null : bind(bindParams),
                rs -> {
                    List<Site> sites = new ArrayList<>();
                    while (rs.next()) {
                        sites.add(mapSite(rs));
                    }
                    return sites;
                }));
    }

    public List<Site> findWithShippingDeclared() {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT * FROM sites WHERE active = 1 AND shipping_status = ? ORDER BY site_code",
                bind(ShippingStatus.DA_KHAI_BAO.name()),
                rs -> {
                    List<Site> sites = new ArrayList<>();
                    while (rs.next()) sites.add(mapSite(rs));
                    return sites;
                }));
    }

    public boolean existsByCode(String siteCode) {
        return findByCode(siteCode).isPresent();
    }

    public void setActive(String siteCode, boolean active) {
        int updated = inJdbcTransaction(connection -> executeUpdate(connection,
                "UPDATE sites SET active = ? WHERE site_code = ?",
                statement -> {
                    JdbcSupport.setBoolean(statement, 1, active);
                    statement.setString(2, siteCode);
                }));
        if (updated == 0) {
            throw new IllegalArgumentException("Site không tồn tại: " + siteCode);
        }
    }

    public void updateMaster(String siteCode, String siteName, String otherInfo) {
        int updated = inJdbcTransaction(connection -> executeUpdate(connection,
                "UPDATE sites SET site_name = ?, other_info = ? WHERE site_code = ?",
                bind(siteName, otherInfo, siteCode)));
        if (updated == 0) {
            throw new IllegalArgumentException("Site không tồn tại: " + siteCode);
        }
    }

    public void updateShipping(String siteCode, int shipDays, int airDays) {
        int updated = inJdbcTransaction(connection -> executeUpdate(connection,
                "UPDATE sites SET ship_days = ?, air_days = ?, shipping_status = ?, shipping_updated_at = ? WHERE site_code = ?",
                statement -> {
                    statement.setInt(1, shipDays);
                    statement.setInt(2, airDays);
                    statement.setString(3, ShippingStatus.DA_KHAI_BAO.name());
                    JdbcSupport.setInstant(statement, 4, Instant.now());
                    statement.setString(5, siteCode);
                }));
        if (updated == 0) {
            throw new IllegalArgumentException("Site không tồn tại: " + siteCode);
        }
    }

    public void delete(String siteCode) {
        inJdbcTransaction(connection -> executeUpdate(connection,
                "DELETE FROM sites WHERE site_code = ?",
                bind(siteCode)));
    }

    private static Site mapSite(java.sql.ResultSet rs) throws java.sql.SQLException {
        Site site = new Site(rs.getString("site_code"), rs.getString("site_name"), rs.getString("other_info"));
        site.setShipDays((Integer) rs.getObject("ship_days"));
        site.setAirDays((Integer) rs.getObject("air_days"));
        site.setActive(JdbcSupport.getBoolean(rs, "active"));
        site.setShippingStatus(JdbcSupport.getShippingStatus(rs, "shipping_status"));
        site.setShippingUpdatedAt(JdbcSupport.getInstant(rs, "shipping_updated_at"));
        return site;
    }
}
