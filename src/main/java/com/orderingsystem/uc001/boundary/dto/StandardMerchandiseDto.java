package com.orderingsystem.uc001.boundary.dto;

import com.orderingsystem.core.domain.StandardMerchandise;

public record StandardMerchandiseDto(
        String merchandiseCode,
        String merchandiseName,
        String description
) {
    public static StandardMerchandiseDto from(StandardMerchandise item) {
        String name = item.getMerchandiseName();
        if (name == null || name.isBlank()) {
            name = item.getMerchandiseCode();
        }
        return new StandardMerchandiseDto(
                item.getMerchandiseCode(),
                name,
                item.getDescription()
        );
    }
}
