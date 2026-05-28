package com.orderingsystem.uc007.boundary.dto;

import java.time.LocalDate;
import java.util.List;

public record MerchandiseSplitPlanDto(
        String merchandiseCode,
        int quantityNeeded,
        LocalDate targetDeliveryDate,
        List<OrderSplitLineDto> lines,
        String errorMessage,
        int shortfall
) {
    public boolean success() {
        return errorMessage == null && shortfall == 0;
    }
}
