package com.orderingsystem.uc007.subsystem;

import com.orderingsystem.core.domain.DeliveryMeans;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MerchandiseAllocationEngineWhiteBoxTest {

    @DisplayName("TC-WB-01: B1 — Q=0")
    @Test
    void tcWb01_b1True_zeroQuantityBranch() {
        var plan = MerchandiseAllocationEngine.allocate(0,
                List.of(pool("S01", 100)), List.of());
        assertTrue(plan.lines().isEmpty());
        assertEquals(0, plan.shortfall());
    }

    @DisplayName("TC-WB-02: B2 — cả hai pools rỗng")
    @Test
    void tcWb02_b2True_bothPoolsEmptyBranch() {
        var plan = MerchandiseAllocationEngine.allocate(50, List.of(), List.of());
        assertTrue(plan.hasNoEligibleSite());
        assertTrue(plan.lines().isEmpty());
    }

    @DisplayName("TC-WB-03: B3+B5(F) — remaining>0, bay bù đủ")
    @Test
    void tcWb03_b3TrueB5False_remainingCoveredByAir() {
        var plan = MerchandiseAllocationEngine.allocate(200,
                List.of(pool("S01", 100)),
                List.of(pool("S02", 200)));
        assertEquals(0, plan.shortfall());
        assertEquals(100, qtyFrom(plan, "S01", DeliveryMeans.SHIP_DELIVERY));
        assertEquals(100, qtyFrom(plan, "S02", DeliveryMeans.AIR_DELIVERY));
    }

    @DisplayName("TC-WB-04: B4 — remaining>0, airPools rỗng")
    @Test
    void tcWb04_b4True_remainingWithEmptyAirPools() {
        var plan = MerchandiseAllocationEngine.allocate(150,
                List.of(pool("S01", 80)), List.of());
        assertTrue(plan.lines().isEmpty());
        assertEquals(70, plan.shortfall());
    }

    @DisplayName("TC-WB-05: B3 — ship stock=0, bay bù")
    @Test
    void tcWb05_b3True_shipZeroStockAirCovers() {
        var plan = MerchandiseAllocationEngine.allocate(50,
                List.of(pool("S01", 0), pool("S02", 0)),
                List.of(pool("S03", 80)));
        assertEquals(0, plan.shortfall());
        assertEquals(50, qtyFrom(plan, "S03", DeliveryMeans.AIR_DELIVERY));
    }

    @DisplayName("TC-WB-10: B5 — bay không bù đủ phần còn thiếu")
    @Test
    void tcWb10_b5True_airCannotCoverRemaining() {
        var plan = MerchandiseAllocationEngine.allocate(250,
                List.of(pool("S01", 100)),
                List.of(pool("S02", 80)));
        assertTrue(plan.lines().isEmpty());
        assertEquals(150, plan.shortfall());
    }

    @DisplayName("TC-WB-06: B8 — không chọn được Site phù hợp")
    @Test
    void tcWb06_b8True_noSiteCanCoverTarget() {
        var result = MerchandiseAllocationEngine.allocateForMode(
                List.of(pool("S01", 0), pool("S02", 0)), 50);
        assertTrue(result.isEmpty());
    }

    @DisplayName("TC-WB-07: B7 — stockPriority, Site lớn nhất đủ Q")
    @Test
    void tcWb07_b7_stockPrioritySortLargestFirst() {
        var plan = MerchandiseAllocationEngine.allocate(100,
                List.of(pool("S01", 100), pool("S02", 80), pool("S03", 60)),
                List.of());
        assertEquals(1, plan.lines().size());
        assertEquals("S01", plan.lines().getFirst().siteCode());
        assertEquals(100, plan.lines().getFirst().quantity());
    }

    @DisplayName("TC-WB-08: B7 — tie stock, ít Site")
    @Test
    void tcWb08_b7_fewerSitesWhenTieStock() {
        var plan = MerchandiseAllocationEngine.allocate(100,
                List.of(pool("S01", 80), pool("S02", 80)),
                List.of());
        assertEquals(0, plan.shortfall());
        assertEquals(100, plan.lines().stream()
                .mapToInt(MerchandiseAllocationEngine.AllocationLine::quantity).sum());
        assertEquals(2, plan.lines().size());
    }

    @DisplayName("TC-WB-09: B6 — allocateForMode target=0")
    @Test
    void tcWb09_b6True_allocateForModeTargetZero() {
        var result = MerchandiseAllocationEngine.allocateForMode(
                List.of(pool("S01", 100)), 0);
        assertTrue(result.isEmpty());
    }

    private static MerchandiseAllocationEngine.SitePool pool(String code, int stock) {
        return new MerchandiseAllocationEngine.SitePool(code, stock);
    }

    private static int qtyFrom(
            MerchandiseAllocationEngine.Plan plan,
            String site,
            DeliveryMeans means
    ) {
        return plan.lines().stream()
                .filter(l -> l.siteCode().equals(site) && l.deliveryMeans() == means)
                .mapToInt(MerchandiseAllocationEngine.AllocationLine::quantity)
                .sum();
    }
}
