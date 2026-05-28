package com.orderingsystem.uc002.boundary.dto;

import com.orderingsystem.core.domain.ImportRequestItem;
import com.orderingsystem.core.domain.ItemStatus;

import java.time.LocalDate;

public record ImportRequestItemDto(
        Long id,
        String merchandiseCode,
        int quantityOrdered,
        String unit,
        LocalDate desiredDeliveryDate,
        ItemStatus itemStatus
) {
    public static ImportRequestItemDto from(ImportRequestItem item) {
        return new ImportRequestItemDto(
                item.getId(),
                item.getMerchandiseCode(),
                item.getQuantityOrdered(),
                item.getUnit(),
                item.getDesiredDeliveryDate(),
                item.getItemStatus()
        );
    }
}
