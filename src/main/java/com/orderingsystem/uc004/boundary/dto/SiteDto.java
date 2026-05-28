package com.orderingsystem.uc004.boundary.dto;

import com.orderingsystem.core.domain.ShippingStatus;
import com.orderingsystem.core.domain.Site;

import java.time.Instant;

/**
 * Dữ liệu Site trả về cho UI — không expose entity JPA.
 */
public record SiteDto(
        String siteCode,
        String siteName,
        String otherInfo,
        boolean active,
        Integer shipDays,
        Integer airDays,
        ShippingStatus shippingStatus,
        Instant shippingUpdatedAt
) {
    public static SiteDto from(Site site) {
        return new SiteDto(
                site.getSiteCode(),
                site.getSiteName(),
                site.getOtherInfo(),
                site.isActive(),
                site.getShipDays(),
                site.getAirDays(),
                site.getShippingStatus(),
                site.getShippingUpdatedAt()
        );
    }
}
