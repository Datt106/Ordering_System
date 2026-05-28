package com.orderingsystem.uc006.boundary.dto;

import com.orderingsystem.core.domain.InventoryQuery;

import java.time.Instant;

public record InventoryQueryDto(
        String queryId,
        String requestId,
        String siteCode,
        String merchandiseCode,
        String unit,
        int inStockQuantity,
        Instant respondedAt,
        boolean pending
) {
    public static InventoryQueryDto from(InventoryQuery query) {
        return new InventoryQueryDto(
                query.getQueryId(),
                query.getRequestId(),
                query.getSiteCode(),
                query.getMerchandiseCode(),
                query.getUnit(),
                query.getInStockQuantity(),
                query.getRespondedAt(),
                query.isPending()
        );
    }
}
