package com.orderingsystem.uc013.boundary.dto;

import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.core.domain.PurchaseOrder;

import java.time.Instant;

public record WarehouseOrderDto(
        String orderId,
        String requestId,
        String siteCode,
        String merchandiseCode,
        int quantityOrdered,
        String unit,
        String deliveryMeans,
        OrderStatus status,
        Instant sentAt,
        Instant confirmedAt,
        Integer actualQuantity,
        Integer quantityDiff
) {
    public static WarehouseOrderDto from(PurchaseOrder order) {
        return new WarehouseOrderDto(
                order.getOrderId(),
                order.getRequestId(),
                order.getSiteCode(),
                order.getMerchandiseCode(),
                order.getQuantityOrdered(),
                order.getUnit(),
                order.getDeliveryMeans().toExternalValue(),
                order.getStatus(),
                order.getSentAt(),
                order.getConfirmedAt(),
                order.getActualQuantity(),
                order.getQuantityDiff()
        );
    }
}
