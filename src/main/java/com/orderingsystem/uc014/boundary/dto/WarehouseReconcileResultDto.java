package com.orderingsystem.uc014.boundary.dto;

import com.orderingsystem.core.domain.OrderStatus;

public record WarehouseReconcileResultDto(
        String orderId,
        int orderedQuantity,
        int actualQuantity,
        int quantityDiff,
        OrderStatus status
) {
}
