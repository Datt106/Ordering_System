package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.StandardMerchandise;

import java.util.List;
import java.util.Optional;

public class MerchandiseCatalogRepository extends BaseRepository {

    public void save(StandardMerchandise item) {
        inTransaction(em -> {
            if (em.find(StandardMerchandise.class, item.getMerchandiseCode()) == null) {
                em.persist(item);
            } else {
                em.merge(item);
            }
        });
    }

    public Optional<StandardMerchandise> findByCode(String merchandiseCode) {
        return query(em -> Optional.ofNullable(em.find(StandardMerchandise.class, merchandiseCode)));
    }

    public void updateInfo(String merchandiseCode, String merchandiseName, String description) {
        inTransaction(em -> {
            StandardMerchandise item = em.find(StandardMerchandise.class, merchandiseCode);
            if (item == null) {
                throw new IllegalArgumentException("Mã hàng không tồn tại trong danh mục chuẩn: " + merchandiseCode);
            }
            item.setMerchandiseName(merchandiseName);
            item.setDescription(description);
        });
    }

    public boolean existsByCode(String merchandiseCode) {
        return query(em -> em.find(StandardMerchandise.class, merchandiseCode) != null);
    }

    public List<StandardMerchandise> findAllOrderByCode() {
        return query(em -> em.createQuery(
                        "SELECT m FROM StandardMerchandise m ORDER BY m.merchandiseCode",
                        StandardMerchandise.class)
                .getResultList());
    }

    public void deleteByCode(String merchandiseCode) {
        inTransaction(em -> {
            StandardMerchandise item = em.find(StandardMerchandise.class, merchandiseCode);
            if (item != null) {
                em.remove(item);
            }
        });
    }

    public boolean isReferenced(String merchandiseCode) {
        return query(em -> {
            Long inRequests = em.createQuery(
                            "SELECT COUNT(i) FROM ImportRequestItem i WHERE i.merchandiseCode = :code",
                            Long.class)
                    .setParameter("code", merchandiseCode)
                    .getSingleResult();
            if (inRequests > 0) {
                return true;
            }
            Long atSites = em.createQuery(
                            "SELECT COUNT(sm) FROM SiteMerchandise sm WHERE sm.merchandiseCode = :code",
                            Long.class)
                    .setParameter("code", merchandiseCode)
                    .getSingleResult();
            if (atSites > 0) {
                return true;
            }
            Long inOrders = em.createQuery(
                            "SELECT COUNT(o) FROM PurchaseOrder o WHERE o.merchandiseCode = :code",
                            Long.class)
                    .setParameter("code", merchandiseCode)
                    .getSingleResult();
            if (inOrders > 0) {
                return true;
            }
            Long inInventory = em.createQuery(
                            "SELECT COUNT(q) FROM InventoryQuery q WHERE q.merchandiseCode = :code",
                            Long.class)
                    .setParameter("code", merchandiseCode)
                    .getSingleResult();
            return inInventory > 0;
        });
    }
}
