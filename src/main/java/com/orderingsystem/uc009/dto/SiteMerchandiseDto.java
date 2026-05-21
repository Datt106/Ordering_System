package com.orderingsystem.uc009.dto;

import com.orderingsystem.domain.site.SiteMerchandise;

import java.time.Instant;

public record SiteMerchandiseDto(
        Long id,
        String siteCode,
        String merchandiseCode,
        Instant updatedAt
) {
    public static SiteMerchandiseDto from(SiteMerchandise entry, String siteCode) {
        return new SiteMerchandiseDto(
                entry.getId(),
                siteCode,
                entry.getMerchandiseCode(),
                entry.getUpdatedAt()
        );
    }
}
