package com.orderingsystem.uc007.dto;

import com.orderingsystem.domain.order.DeliveryMeans;

public record OrderSplitLineDto(
        String siteCode,
        String merchandiseCode,
        int quantity,
        String unit,
        DeliveryMeans deliveryMeans,
        String deliveryMeansLabel
) {
    public static OrderSplitLineDto of(
            String siteCode,
            String merchandiseCode,
            int quantity,
            String unit,
            DeliveryMeans deliveryMeans
    ) {
        return new OrderSplitLineDto(
                siteCode,
                merchandiseCode,
                quantity,
                unit,
                deliveryMeans,
                deliveryMeans.toExternalValue()
        );
    }
}
