package com.orderingsystem.uc007.dto;

import java.time.LocalDate;
import java.util.List;

public record OrderSplitResultDto(
        String requestId,
        LocalDate calculationStartDate,
        List<MerchandiseSplitPlanDto> plans,
        List<OrderSplitLineDto> allLines,
        boolean readyToConfirm
) {
    public boolean allMerchandiseSucceeded() {
        return plans.stream().allMatch(MerchandiseSplitPlanDto::success);
    }
}
