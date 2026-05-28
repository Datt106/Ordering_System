package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.ShippingStatus;
import com.orderingsystem.core.domain.Site;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class SiteRepository extends BaseRepository {

    public void save(Site site) {
        inTransaction(em -> {
            if (em.find(Site.class, site.getSiteCode()) == null) {
                em.persist(site);
            } else {
                em.merge(site);
            }
        });
    }

    public Optional<Site> findByCode(String siteCode) {
        return query(em -> Optional.ofNullable(em.find(Site.class, siteCode)));
    }

    public List<Site> findAll() {
        return query(em -> em.createQuery("SELECT s FROM Site s ORDER BY s.siteCode", Site.class)
                .getResultList());
    }

    public List<Site> findAllActive() {
        return query(em -> em.createQuery(
                        "SELECT s FROM Site s WHERE s.active = true ORDER BY s.siteCode", Site.class)
                .getResultList());
    }

    /** Site đã khai báo vận chuyển (UC010) — dùng cho UC006/UC007. */
    public List<Site> findWithShippingDeclared() {
        return query(em -> em.createQuery(
                        "SELECT s FROM Site s WHERE s.active = true AND s.shippingStatus = :status "
                                + "ORDER BY s.siteCode",
                        Site.class)
                .setParameter("status", ShippingStatus.DA_KHAI_BAO)
                .getResultList());
    }

    public boolean existsByCode(String siteCode) {
        return findByCode(siteCode).isPresent();
    }

    /** UC004 — chỉ cập nhật hồ sơ master. */
    public void updateMaster(String siteCode, String siteName, String otherInfo) {
        inTransaction(em -> {
            Site site = em.find(Site.class, siteCode);
            if (site == null) {
                throw new IllegalArgumentException("Site không tồn tại: " + siteCode);
            }
            site.setSiteName(siteName);
            site.setOtherInfo(otherInfo);
        });
    }

    /** UC010 — chỉ Site cập nhật lead time. */
    public void updateShipping(String siteCode, int shipDays, int airDays) {
        inTransaction(em -> {
            Site site = em.find(Site.class, siteCode);
            if (site == null) {
                throw new IllegalArgumentException("Site không tồn tại: " + siteCode);
            }
            site.setShipDays(shipDays);
            site.setAirDays(airDays);
            site.setShippingStatus(ShippingStatus.DA_KHAI_BAO);
            site.setShippingUpdatedAt(Instant.now());
        });
    }

    public void delete(String siteCode) {
        inTransaction(em -> {
            Site site = em.find(Site.class, siteCode);
            if (site != null) {
                em.remove(site);
            }
        });
    }
}
