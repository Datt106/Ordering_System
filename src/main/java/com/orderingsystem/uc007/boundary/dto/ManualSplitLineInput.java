package com.orderingsystem.uc007.boundary.dto;

import com.orderingsystem.core.domain.DeliveryMeans;

/** Một dòng phương án do nhân viên Overseas nhập/chỉnh tay trước khi xác nhận. */
public record ManualSplitLineInput(
        String siteCode,
        String merchandiseCode,
        int quantity,
        DeliveryMeans deliveryMeans
) {
}
