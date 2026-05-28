package com.orderingsystem.uc008.boundary.dto;

import com.orderingsystem.core.domain.OrderStatus;

import java.time.Instant;
import java.util.List;

/** Kết quả gửi đơn UC008. */
public record OrderDispatchResultDto(
        String requestId,
        int totalOrders,
        int sentOrders,
        Instant sentAt,
        List<OrderDispatchLineDto> lines
) {
    public record OrderDispatchLineDto(
            String orderId,
            String siteCode,
            String merchandiseCode,
            int quantityOrdered,
            String unit,
            String deliveryMeans,
            OrderStatus status
    ) {}
}
