package com.orderingsystem.uc002.dto;

import java.time.LocalDate;

/** Một dòng mặt hàng khi Sales tạo yêu cầu (UC002). */
public record CreateImportRequestLineInput(
        String merchandiseCode,
        int quantityOrdered,
        String unit,
        LocalDate desiredDeliveryDate
) {
}
