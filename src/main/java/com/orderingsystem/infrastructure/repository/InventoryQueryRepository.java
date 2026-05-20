package com.orderingsystem.infrastructure.repository;

import com.orderingsystem.domain.inventory.InventoryQuery;

import java.util.List;

public class InventoryQueryRepository extends BaseRepository {

    public void save(InventoryQuery query) {
        inTransaction(em -> {
            if (em.find(InventoryQuery.class, query.getQueryId()) == null) {
                em.persist(query);
            } else {
                em.merge(query);
            }
        });
    }

    public void saveAll(List<InventoryQuery> queries) {
        inTransaction(em -> {
            for (InventoryQuery q : queries) {
                if (em.find(InventoryQuery.class, q.getQueryId()) == null) {
                    em.persist(q);
                } else {
                    em.merge(q);
                }
            }
        });
    }

    public List<InventoryQuery> findByRequestId(String requestId) {
        return query(em -> em.createQuery(
                        "SELECT q FROM InventoryQuery q WHERE q.requestId = :requestId "
                                + "ORDER BY q.siteCode, q.merchandiseCode",
                        InventoryQuery.class)
                .setParameter("requestId", requestId)
                .getResultList());
    }

    public void deleteByRequestId(String requestId) {
        inTransaction(em -> {
            em.createQuery("DELETE FROM InventoryQuery q WHERE q.requestId = :requestId")
                    .setParameter("requestId", requestId)
                    .executeUpdate();
        });
    }
}
