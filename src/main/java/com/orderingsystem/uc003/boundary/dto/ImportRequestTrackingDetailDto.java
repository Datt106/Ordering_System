package com.orderingsystem.uc003.boundary.dto;

import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;

import java.util.List;

/** Chi tiết yêu cầu + đơn con (UC003 — FR-02.3). */
public record ImportRequestTrackingDetailDto(
        ImportRequestDto request,
        List<PurchaseOrderTrackingDto> childOrders
) {
}
