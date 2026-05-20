package com.orderingsystem.infrastructure.repository;

import com.orderingsystem.domain.order.OrderStatus;
import com.orderingsystem.domain.order.PurchaseOrder;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PurchaseOrderRepository extends BaseRepository {

    private static final Set<OrderStatus> ACTIVE_ORDER_STATUSES = EnumSet.of(
            OrderStatus.CHO_GUI,
            OrderStatus.DA_GUI,
            OrderStatus.DA_XAC_NHAN,
            OrderStatus.SAI_LECH
    );

    public void save(PurchaseOrder order) {
        inTransaction(em -> {
            if (em.find(PurchaseOrder.class, order.getOrderId()) == null) {
                em.persist(order);
            } else {
                em.merge(order);
            }
        });
    }

    public void saveAll(List<PurchaseOrder> orders) {
        inTransaction(em -> {
            for (PurchaseOrder order : orders) {
                if (em.find(PurchaseOrder.class, order.getOrderId()) == null) {
                    em.persist(order);
                } else {
                    em.merge(order);
                }
            }
        });
    }

    public Optional<PurchaseOrder> findById(String orderId) {
        return query(em -> Optional.ofNullable(em.find(PurchaseOrder.class, orderId)));
    }

    public List<PurchaseOrder> findByRequestId(String requestId) {
        return query(em -> em.createQuery(
                        "SELECT o FROM PurchaseOrder o WHERE o.requestId = :requestId "
                                + "ORDER BY o.siteCode, o.merchandiseCode",
                        PurchaseOrder.class)
                .setParameter("requestId", requestId)
                .getResultList());
    }

    public List<PurchaseOrder> findByStatus(OrderStatus status) {
        return query(em -> em.createQuery(
                        "SELECT o FROM PurchaseOrder o WHERE o.status = :status "
                                + "ORDER BY o.sentAt DESC",
                        PurchaseOrder.class)
                .setParameter("status", status)
                .getResultList());
    }

    public List<PurchaseOrder> findByStatuses(Set<OrderStatus> statuses) {
        return query(em -> em.createQuery(
                        "SELECT o FROM PurchaseOrder o WHERE o.status IN :statuses "
                                + "ORDER BY o.requestId, o.siteCode",
                        PurchaseOrder.class)
                .setParameter("statuses", statuses)
                .getResultList());
    }

    /** UC004 — từ chối xóa Site nếu còn đơn chưa hoàn tất. */
    public boolean hasActiveOrdersForSite(String siteCode) {
        return query(em -> {
            Long count = em.createQuery(
                            "SELECT COUNT(o) FROM PurchaseOrder o "
                                    + "WHERE o.siteCode = :siteCode AND o.status IN :statuses",
                            Long.class)
                    .setParameter("siteCode", siteCode)
                    .setParameter("statuses", ACTIVE_ORDER_STATUSES)
                    .getSingleResult();
            return count > 0;
        });
    }
}
