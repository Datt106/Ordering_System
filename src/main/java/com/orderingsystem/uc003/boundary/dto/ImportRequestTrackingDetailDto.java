package com.orderingsystem.uc003.boundary.dto;

import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;

import java.util.List;
import java.util.Objects;

/** Chi tiết yêu cầu + đơn con (UC003 — FR-02.3). */
public record ImportRequestTrackingDetailDto(
        ImportRequestDto request,
        List<PurchaseOrderTrackingDto> childOrders
) {
    public ImportRequestTrackingDetailDto {
        Objects.requireNonNull(request, "request");
        childOrders = List.copyOf(childOrders);
    }
}
