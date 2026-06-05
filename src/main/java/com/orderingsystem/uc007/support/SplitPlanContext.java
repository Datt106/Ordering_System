package com.orderingsystem.uc007.support;

import com.orderingsystem.core.domain.Site;

import java.time.LocalDate;
import java.util.Map;

/** Dữ liệu đã load sẵn cho phân bổ / validate — không gọi DAO trong subsystem. */
public record SplitPlanContext(
        String requestId,
        LocalDate startDate,
        boolean inventoryReady,
        Map<String, MerchandiseDemand> demands,
        Map<String, Site> sitesByCode,
        Map<String, Map<String, StockSnapshot>> stockBySiteMerchandise
) {
}
