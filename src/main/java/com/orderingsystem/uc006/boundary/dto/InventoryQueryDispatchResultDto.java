package com.orderingsystem.uc006.boundary.dto;

import java.util.List;

/** Kết quả gửi truy vấn tồn kho (UC006). */
public record InventoryQueryDispatchResultDto(
        String requestId,
        int totalQueries,
        int pendingQueries,
        List<SiteInventoryQueryGroupDto> siteGroups,
        List<MerchandiseQueryErrorDto> merchandiseErrors
) {
    public boolean allSitesResponded() {
        return totalQueries > 0 && pendingQueries == 0;
    }
}
