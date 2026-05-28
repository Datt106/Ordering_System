package com.orderingsystem.uc009.boundary.dto;

import com.orderingsystem.core.domain.SiteMerchandise;

import java.time.Instant;

public record SiteMerchandiseDto(
        Long id,
        String siteCode,
        String merchandiseCode,
        String merchandiseName,
        String description,
        Instant updatedAt
) {
    public static SiteMerchandiseDto from(
            SiteMerchandise entry,
            String siteCode,
            String merchandiseName,
            String description
    ) {
        return new SiteMerchandiseDto(
                entry.getId(),
                siteCode,
                entry.getMerchandiseCode(),
                merchandiseName,
                description,
                entry.getUpdatedAt()
        );
    }
}
