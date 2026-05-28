package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.Site;
import com.orderingsystem.core.domain.SiteMerchandise;

import java.util.List;
import java.util.Optional;

public class SiteMerchandiseRepository extends BaseRepository {

    public void save(SiteMerchandise entry) {
        inTransaction(em -> {
            if (entry.getId() == null) {
                em.persist(entry);
            } else {
                em.merge(entry);
            }
        });
    }

    public List<SiteMerchandise> findBySiteCode(String siteCode) {
        return query(em -> em.createQuery(
                        "SELECT sm FROM SiteMerchandise sm "
                                + "WHERE sm.site.siteCode = :siteCode "
                                + "ORDER BY sm.merchandiseCode",
                        SiteMerchandise.class)
                .setParameter("siteCode", siteCode)
                .getResultList());
    }

    public List<String> findMerchandiseCodesBySiteCode(String siteCode) {
        return query(em -> em.createQuery(
                        "SELECT sm.merchandiseCode FROM SiteMerchandise sm "
                                + "WHERE sm.site.siteCode = :siteCode "
                                + "ORDER BY sm.merchandiseCode",
                        String.class)
                .setParameter("siteCode", siteCode)
                .getResultList());
    }

    /** UC006 — Site nào kinh doanh mặt hàng M. */
    public List<String> findSiteCodesByMerchandiseCode(String merchandiseCode) {
        return query(em -> em.createQuery(
                        "SELECT sm.site.siteCode FROM SiteMerchandise sm "
                                + "WHERE sm.merchandiseCode = :code "
                                + "ORDER BY sm.site.siteCode",
                        String.class)
                .setParameter("code", merchandiseCode)
                .getResultList());
    }

    public Optional<SiteMerchandise> findBySiteAndMerchandise(String siteCode, String merchandiseCode) {
        return query(em -> {
            List<SiteMerchandise> results = em.createQuery(
                            "SELECT sm FROM SiteMerchandise sm "
                                    + "WHERE sm.site.siteCode = :siteCode "
                                    + "AND sm.merchandiseCode = :merchandiseCode",
                            SiteMerchandise.class)
                    .setParameter("siteCode", siteCode)
                    .setParameter("merchandiseCode", merchandiseCode)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
        });
    }

    public void deleteById(Long id) {
        inTransaction(em -> {
            SiteMerchandise entry = em.find(SiteMerchandise.class, id);
            if (entry != null) {
                em.remove(entry);
            }
        });
    }

    public void deleteAllBySiteCode(String siteCode) {
        inTransaction(em -> {
            em.createQuery("DELETE FROM SiteMerchandise sm WHERE sm.site.siteCode = :siteCode")
                    .setParameter("siteCode", siteCode)
                    .executeUpdate();
        });
    }

    public SiteMerchandise createLink(Site site, String merchandiseCode) {
        return inTransaction(em -> {
            Site managedSite = em.getReference(Site.class, site.getSiteCode());
            SiteMerchandise entry = new SiteMerchandise(managedSite, merchandiseCode);
            em.persist(entry);
            return entry;
        });
    }
}
