package com.orderingsystem.uc007.subsystem;

import com.orderingsystem.core.domain.Site;
import com.orderingsystem.uc007.support.MerchandiseDemand;
import com.orderingsystem.uc007.support.SplitPlanContext;
import com.orderingsystem.uc007.support.StockSnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Lọc Site đáp ứng ETA tàu/bay theo ngày nhận mong muốn. */
public final class SiteFilter {

    public record SitePools(
            List<MerchandiseAllocationEngine.SitePool> shipPools,
            List<MerchandiseAllocationEngine.SitePool> airPools
    ) {
    }

    private SiteFilter() {
    }

    public static SitePools eligiblePools(SplitPlanContext context, MerchandiseDemand demand) {
        List<MerchandiseAllocationEngine.SitePool> shipPools = new ArrayList<>();
        List<MerchandiseAllocationEngine.SitePool> airPools = new ArrayList<>();
        LocalDate startDate = context.startDate();

        for (Map.Entry<String, Map<String, StockSnapshot>> siteEntry : context.stockBySiteMerchandise().entrySet()) {
            StockSnapshot stock = siteEntry.getValue().get(demand.merchandiseCode());
            if (stock == null || !stock.responded() || stock.quantity() <= 0) {
                continue;
            }
            Site site = context.sitesByCode().get(siteEntry.getKey());
            if (site == null || site.getShipDays() == null || site.getAirDays() == null) {
                continue;
            }
            LocalDate etaShip = startDate.plusDays(site.getShipDays());
            LocalDate etaAir = startDate.plusDays(site.getAirDays());
            var pool = new MerchandiseAllocationEngine.SitePool(siteEntry.getKey(), stock.quantity());
            if (!etaShip.isAfter(demand.targetDate())) {
                shipPools.add(pool);
            }
            if (!etaAir.isAfter(demand.targetDate())) {
                airPools.add(pool);
            }
        }
        return new SitePools(shipPools, airPools);
    }
}
