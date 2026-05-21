package com.orderingsystem.infrastructure.repository;

import com.orderingsystem.domain.catalog.StandardMerchandise;

public class MerchandiseCatalogRepository extends BaseRepository {

    public void save(StandardMerchandise item) {
        inTransaction(em -> {
            if (em.find(StandardMerchandise.class, item.getMerchandiseCode()) == null) {
                em.persist(item);
            }
        });
    }

    public boolean existsByCode(String merchandiseCode) {
        return query(em -> em.find(StandardMerchandise.class, merchandiseCode) != null);
    }
}
