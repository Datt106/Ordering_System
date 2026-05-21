package com.orderingsystem.uc003.dto;

import com.orderingsystem.uc002.dto.ImportRequestDto;

import java.util.List;

/** Chi tiết yêu cầu + đơn con (UC003 — FR-02.3). */
public record ImportRequestTrackingDetailDto(
        ImportRequestDto request,
        List<PurchaseOrderTrackingDto> childOrders
) {
}
