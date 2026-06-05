package com.orderingsystem.uc013.boundary.dto;

import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.core.domain.PurchaseOrder;

import java.time.Instant;

public record WarehouseOrderDto(
        String orderId,
        String requestId,
        String siteCode,
        String siteName,
        String merchandiseCode,
        String merchandiseName,
        int quantityOrdered,
        String unit,
        String deliveryMeans,
        OrderStatus status,
        Instant sentAt,
        Instant confirmedAt,
        Instant reconciledAt,
        Integer actualQuantity,
        Integer quantityDiff
) {
    public static WarehouseOrderDto from(PurchaseOrder order) {
        return new WarehouseOrderDto(
                order.getOrderId(),
                order.getRequestId(),
                order.getSiteCode(),
                order.getSiteName(),
                order.getMerchandiseCode(),
                order.getMerchandiseName(),
                order.getQuantityOrdered(),
                order.getUnit(),
                order.getDeliveryMeans().toExternalValue(),
                order.getStatus(),
                order.getSentAt(),
                order.getConfirmedAt(),
                order.getReconciledAt(),
                order.getActualQuantity(),
                order.getQuantityDiff()
        );
    }
}