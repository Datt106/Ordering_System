package com.orderingsystem.infrastructure.repository;

import com.orderingsystem.domain.request.ImportRequest;
import com.orderingsystem.domain.request.RequestStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ImportRequestRepository extends BaseRepository {

    public void save(ImportRequest request) {
        inTransaction(em -> {
            if (em.find(ImportRequest.class, request.getRequestId()) == null) {
                em.persist(request);
            } else {
                em.merge(request);
            }
        });
    }

    public Optional<ImportRequest> findById(String requestId) {
        return query(em -> Optional.ofNullable(em.find(ImportRequest.class, requestId)));
    }

    public Optional<ImportRequest> findByIdWithItems(String requestId) {
        return query(em -> {
            List<ImportRequest> results = em.createQuery(
                            "SELECT DISTINCT r FROM ImportRequest r "
                                    + "LEFT JOIN FETCH r.items "
                                    + "WHERE r.requestId = :id",
                            ImportRequest.class)
                    .setParameter("id", requestId)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
        });
    }

    /** Đếm yêu cầu có mã bắt đầu bằng prefix (vd. REQ-20260521-) — sinh sequence UC002. */
    public int countByRequestIdPrefix(String prefix) {
        return query(em -> {
            Long count = em.createQuery(
                            "SELECT COUNT(r) FROM ImportRequest r WHERE r.requestId LIKE :pattern",
                            Long.class)
                    .setParameter("pattern", prefix + "%")
                    .getSingleResult();
            return count.intValue();
        });
    }

    public List<ImportRequest> findByStatus(RequestStatus status) {
        return query(em -> em.createQuery(
                        "SELECT r FROM ImportRequest r WHERE r.status = :status "
                                + "ORDER BY r.createdAt DESC",
                        ImportRequest.class)
                .setParameter("status", status)
                .getResultList());
    }

    public void updateStatus(String requestId, RequestStatus status, String processedBy) {
        inTransaction(em -> {
            ImportRequest request = em.find(ImportRequest.class, requestId);
            if (request == null) {
                throw new IllegalArgumentException("Yêu cầu không tồn tại: " + requestId);
            }
            request.setStatus(status);
            if (processedBy != null) {
                request.setProcessedBy(processedBy);
                request.setProcessedAt(Instant.now());
            }
        });
    }
}
