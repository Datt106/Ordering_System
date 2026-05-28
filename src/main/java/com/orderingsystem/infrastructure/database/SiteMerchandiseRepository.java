package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.Site;
import com.orderingsystem.core.domain.SiteMerchandise;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SiteMerchandiseRepository extends BaseRepository {

    public void save(SiteMerchandise entry) {
        if (entry.getId() == null) {
            inJdbcTransaction(connection -> {
                executeUpdate(connection,
                        "INSERT INTO site_merchandise (site_code, merchandise_code, updated_at) VALUES (?, ?, ?)",
                        statement -> {
                            statement.setString(1, entry.getSite().getSiteCode());
                            statement.setString(2, entry.getMerchandiseCode());
                            JdbcSupport.setInstant(statement, 3, entry.getUpdatedAt());
                        });
                Long id = executeQuery(connection, "SELECT last_insert_rowid()", null, rs -> {
                    rs.next();
                    return rs.getLong(1);
                });
                entry.setId(id);
                return null;
            });
        } else {
            inJdbcTransaction(connection -> executeUpdate(connection,
                    "UPDATE site_merchandise SET site_code = ?, merchandise_code = ?, updated_at = ? WHERE id = ?",
                    statement -> {
                        statement.setString(1, entry.getSite().getSiteCode());
                        statement.setString(2, entry.getMerchandiseCode());
                        JdbcSupport.setInstant(statement, 3, entry.getUpdatedAt());
                        statement.setLong(4, entry.getId());
                    }));
        }
    }

    public List<SiteMerchandise> findBySiteCode(String siteCode) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT sm.*, s.site_name, s.ship_days, s.air_days, s.other_info, s.active, s.shipping_status, s.shipping_updated_at "
                        + "FROM site_merchandise sm JOIN sites s ON s.site_code = sm.site_code "
                        + "WHERE sm.site_code = ? ORDER BY sm.merchandise_code",
                bind(siteCode),
                rs -> {
                    List<SiteMerchandise> items = new ArrayList<>();
                    while (rs.next()) items.add(mapSiteMerchandise(rs));
                    return items;
                }));
    }

    public List<String> findMerchandiseCodesBySiteCode(String siteCode) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT merchandise_code FROM site_merchandise WHERE site_code = ? ORDER BY merchandise_code",
                bind(siteCode),
                rs -> {
                    List<String> codes = new ArrayList<>();
                    while (rs.next()) codes.add(rs.getString(1));
                    return codes;
                }));
    }

    public List<String> findSiteCodesByMerchandiseCode(String merchandiseCode) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT site_code FROM site_merchandise WHERE merchandise_code = ? ORDER BY site_code",
                bind(merchandiseCode),
                rs -> {
                    List<String> codes = new ArrayList<>();
                    while (rs.next()) codes.add(rs.getString(1));
                    return codes;
                }));
    }

    public Optional<SiteMerchandise> findBySiteAndMerchandise(String siteCode, String merchandiseCode) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT sm.*, s.site_name, s.ship_days, s.air_days, s.other_info, s.active, s.shipping_status, s.shipping_updated_at "
                        + "FROM site_merchandise sm JOIN sites s ON s.site_code = sm.site_code "
                        + "WHERE sm.site_code = ? AND sm.merchandise_code = ? LIMIT 1",
                bind(siteCode, merchandiseCode),
                rs -> rs.next() ? Optional.of(mapSiteMerchandise(rs)) : Optional.empty()));
    }

    public void deleteById(Long id) {
        inJdbcTransaction(connection -> executeUpdate(connection,
                "DELETE FROM site_merchandise WHERE id = ?",
                bind(id)));
    }

    public void deleteAllBySiteCode(String siteCode) {
        inJdbcTransaction(connection -> executeUpdate(connection,
                "DELETE FROM site_merchandise WHERE site_code = ?",
                bind(siteCode)));
    }

    public SiteMerchandise createLink(Site site, String merchandiseCode) {
        SiteMerchandise entry = new SiteMerchandise(site, merchandiseCode);
        entry.setUpdatedAt(Instant.now());
        save(entry);
        return entry;
    }

    private static SiteMerchandise mapSiteMerchandise(java.sql.ResultSet rs) throws java.sql.SQLException {
        Site site = new Site(rs.getString("site_code"), rs.getString("site_name"), rs.getString("other_info"));
        site.setShipDays((Integer) rs.getObject("ship_days"));
        site.setAirDays((Integer) rs.getObject("air_days"));
        site.setActive(JdbcSupport.getBoolean(rs, "active"));
        site.setShippingStatus(JdbcSupport.getShippingStatus(rs, "shipping_status"));
        site.setShippingUpdatedAt(JdbcSupport.getInstant(rs, "shipping_updated_at"));

        SiteMerchandise sm = new SiteMerchandise(site, rs.getString("merchandise_code"));
        sm.setId(rs.getLong("id"));
        sm.setUpdatedAt(JdbcSupport.getInstant(rs, "updated_at"));
        return sm;
    }
}
