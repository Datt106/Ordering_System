package com.orderingsystem.uc003.dto;

import com.orderingsystem.domain.order.DeliveryMeans;
import com.orderingsystem.domain.order.OrderStatus;
import com.orderingsystem.domain.order.PurchaseOrder;

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
        LocalDate expectedDeliveryDate
) {
    public static PurchaseOrderTrackingDto from(PurchaseOrder order, LocalDate expectedDeliveryDate) {
        return new PurchaseOrderTrackingDto(
                order.getOrderId(),
                order.getSiteCode(),
                order.getMerchandiseCode(),
                order.getQuantityOrdered(),
                order.getUnit(),
                order.getDeliveryMeans(),
                order.getDeliveryMeans().toExternalValue(),
                order.getStatus(),
                expectedDeliveryDate
        );
    }
}
