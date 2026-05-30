package com.orderingsystem.uc007.boundary.dto;

import java.util.List;

/** Kết quả kiểm tra phương án chỉnh tay — preview chỉ có khi hợp lệ. */
public record ManualSplitValidationResultDto(
        boolean valid,
        List<String> errors,
        OrderSplitResultDto preview
) {
    public static ManualSplitValidationResultDto invalid(List<String> errors) {
        return new ManualSplitValidationResultDto(false, List.copyOf(errors), null);
    }

    public static ManualSplitValidationResultDto ok(OrderSplitResultDto preview) {
        return new ManualSplitValidationResultDto(true, List.of(), preview);
    }
}
