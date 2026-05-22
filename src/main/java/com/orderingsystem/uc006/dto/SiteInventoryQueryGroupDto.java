package com.orderingsystem.uc006.dto;

import java.util.List;

/** Nhóm truy vấn gửi tới một Site */
public record SiteInventoryQueryGroupDto(
        String siteCode,
        String siteName,
        Integer shipDays,
        Integer airDays,
        List<InventoryQueryDto> lines
) {
}
