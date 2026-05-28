package com.orderingsystem.uc012.boundary.dto;

import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.core.domain.PurchaseOrder;

import java.time.Instant;

public record SiteOrderDto(
        String orderId,
        String requestId,
        String siteCode,
        String merchandiseCode,
        int quantityOrdered,
        String unit,
        String deliveryMeans,
        OrderStatus status,
        Instant sentAt,
        Instant confirmedAt
) {
    public static SiteOrderDto from(PurchaseOrder order) {
        return new SiteOrderDto(
                order.getOrderId(),
                order.getRequestId(),
                order.getSiteCode(),
                order.getMerchandiseCode(),
                order.getQuantityOrdered(),
                order.getUnit(),
                order.getDeliveryMeans().toExternalValue(),
                order.getStatus(),
                order.getSentAt(),
                order.getConfirmedAt()
        );
    }
}
