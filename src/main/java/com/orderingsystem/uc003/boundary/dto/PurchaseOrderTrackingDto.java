package com.orderingsystem.uc003.boundary.dto;

import com.orderingsystem.core.domain.DeliveryMeans;
import com.orderingsystem.core.domain.OrderStatus;

import java.time.LocalDate;

/** Đơn hàng con hiển thị cho Sales khi theo dõi yêu cầu (UC003). */
public record PurchaseOrderTrackingDto(
        String orderId,
        String siteCode,
        String merchandiseCode,
        int quantityOrdered,
        String unit,
        DeliveryMeans deliveryMeans,
        String deliveryMeansLabel,
        OrderStatus orderStatus,
        LocalDate expectedDeliveryDate,
        Integer actualQuantity,
        Integer quantityDiff
) {
}
