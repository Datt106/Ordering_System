package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.InventoryQuery;

import java.util.List;
import java.util.Optional;

public class InventoryQueryRepository extends BaseRepository {

    public Optional<InventoryQuery> findById(String queryId) {
        return query(em -> Optional.ofNullable(em.find(InventoryQuery.class, queryId)));
    }

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

    public List<InventoryQuery> findPendingBySiteCode(String siteCode) {
        return query(em -> em.createQuery(
                        "SELECT q FROM InventoryQuery q WHERE q.siteCode = :siteCode "
                                + "AND q.respondedAt IS NULL ORDER BY q.requestId, q.merchandiseCode",
                        InventoryQuery.class)
                .setParameter("siteCode", siteCode)
                .getResultList());
    }

    public long countByRequestId(String requestId) {
        return query(em -> em.createQuery(
                        "SELECT COUNT(q) FROM InventoryQuery q WHERE q.requestId = :requestId",
                        Long.class)
                .setParameter("requestId", requestId)
                .getSingleResult());
    }

    public long countRespondedByRequestId(String requestId) {
        return query(em -> em.createQuery(
                        "SELECT COUNT(q) FROM InventoryQuery q WHERE q.requestId = :requestId "
                                + "AND q.respondedAt IS NOT NULL",
                        Long.class)
                .setParameter("requestId", requestId)
                .getSingleResult());
    }
}
