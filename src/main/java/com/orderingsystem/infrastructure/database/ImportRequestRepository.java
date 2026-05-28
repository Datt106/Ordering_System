package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.ImportRequestItem;
import com.orderingsystem.core.domain.ItemStatus;
import com.orderingsystem.core.domain.RequestStatus;

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

    public List<ImportRequest> findByDepartment(String department) {
        return query(em -> em.createQuery(
                        "SELECT r FROM ImportRequest r WHERE r.department = :department "
                                + "ORDER BY r.createdAt DESC",
                        ImportRequest.class)
                .setParameter("department", department)
                .getResultList());
    }

    /** UC003 — lọc theo trạng thái và/hoặc khoảng ngày tạo (theo ngày lịch, inclusive). */
    public List<ImportRequest> findByDepartmentFiltered(
            String department,
            RequestStatus status,
            Instant createdFromInclusive,
            Instant createdToExclusive
    ) {
        return query(em -> {
            StringBuilder jpql = new StringBuilder(
                    "SELECT r FROM ImportRequest r WHERE r.department = :department");
            if (status != null) {
                jpql.append(" AND r.status = :status");
            }
            if (createdFromInclusive != null) {
                jpql.append(" AND r.createdAt >= :createdFrom");
            }
            if (createdToExclusive != null) {
                jpql.append(" AND r.createdAt < :createdTo");
            }
            jpql.append(" ORDER BY r.createdAt DESC");

            var query = em.createQuery(jpql.toString(), ImportRequest.class)
                    .setParameter("department", department);
            if (status != null) {
                query.setParameter("status", status);
            }
            if (createdFromInclusive != null) {
                query.setParameter("createdFrom", createdFromInclusive);
            }
            if (createdToExclusive != null) {
                query.setParameter("createdTo", createdToExclusive);
            }
            return query.getResultList();
        });
    }

    public long countItemsByRequestId(String requestId) {
        return query(em -> em.createQuery(
                        "SELECT COUNT(i) FROM ImportRequestItem i WHERE i.request.requestId = :requestId",
                        Long.class)
                .setParameter("requestId", requestId)
                .getSingleResult());
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

    /** UC006 — đánh dấu dòng yêu cầu khi không có Site kinh doanh mặt hàng. */
    public void updateItemStatus(long itemId, ItemStatus status) {
        inTransaction(em -> {
            ImportRequestItem item = em.find(ImportRequestItem.class, itemId);
            if (item == null) {
                throw new IllegalArgumentException("Dòng yêu cầu không tồn tại: " + itemId);
            }
            item.setItemStatus(status);
        });
    }

    /** UC005 — chỉ tiếp nhận yêu cầu đang Chờ xử lý. */
    public void acceptForProcessing(String requestId, String processedBy) {
        inTransaction(em -> {
            ImportRequest request = em.find(ImportRequest.class, requestId);
            if (request == null) {
                throw new IllegalArgumentException("Yêu cầu không tồn tại: " + requestId);
            }
            if (request.getStatus() != RequestStatus.CHO_XU_LY) {
                throw new IllegalStateException(
                        "Chỉ tiếp nhận được yêu cầu ở trạng thái Chờ xử lý. Hiện tại: " + request.getStatus());
            }
            request.setStatus(RequestStatus.DANG_XU_LY);
            request.setProcessedBy(processedBy);
            request.setProcessedAt(Instant.now());
        });
    }
}
