package com.orderingsystem.uc007.subsystem;

import com.orderingsystem.core.domain.DeliveryMeans;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MerchandiseAllocationEngineBlackBoxTest {

    @DisplayName("TC-BB-01: Q=0 — không phân bổ")
    @Test
    void tcBb01_zeroQuantityReturnsEmptyPlan() {
        var plan = MerchandiseAllocationEngine.allocate(0,
                List.of(pool("S01", 150)), List.of());
        assertFalse(plan.hasNoEligibleSite());
        assertEquals(0, plan.shortfall());
        assertTrue(plan.lines().isEmpty());
    }

    @DisplayName("TC-BB-02: Q âm — không phân bổ")
    @Test
    void tcBb02_negativeQuantityReturnsEmptyPlan() {
        var plan = MerchandiseAllocationEngine.allocate(-5,
                List.of(pool("S01", 150)), List.of());
        assertEquals(0, plan.shortfall());
        assertTrue(plan.lines().isEmpty());
    }

    @DisplayName("TC-BB-03: Không Site eligible")
    @Test
    void tcBb03_emptyPoolsReturnNoEligibleSite() {
        var plan = MerchandiseAllocationEngine.allocate(100, List.of(), List.of());
        assertTrue(plan.hasNoEligibleSite());
        assertTrue(plan.lines().isEmpty());
        assertEquals(0, plan.shortfall());
    }

    @DisplayName("TC-BB-04: Một Site tàu đủ số lượng")
    @Test
    void tcBb04_singleSiteShipCoversAll() {
        var plan = MerchandiseAllocationEngine.allocate(150,
                List.of(pool("S01", 150)), List.of());
        assertEquals(0, plan.shortfall());
        assertEquals(1, plan.lines().size());
        assertLine(plan.lines().getFirst(), "S01", DeliveryMeans.SHIP_DELIVERY, 150);
    }

    @DisplayName("TC-BB-05: Tàu thiếu, bay bù đủ")
    @Test
    void tcBb05_shipThenAirCoversAll() {
        var plan = MerchandiseAllocationEngine.allocate(250,
                List.of(pool("S01", 100)),
                List.of(pool("S03", 200)));
        assertEquals(0, plan.shortfall());
        assertEquals(250, totalQty(plan));
        assertEquals(100, qtyFrom(plan, "S01", DeliveryMeans.SHIP_DELIVERY));
        assertEquals(150, qtyFrom(plan, "S03", DeliveryMeans.AIR_DELIVERY));
    }

    @DisplayName("TC-BB-06: Ưu tiên tàu dù có bay")
    @Test
    void tcBb06_prefersShipOverAirWhenShipSuffices() {
        var plan = MerchandiseAllocationEngine.allocate(100,
                List.of(pool("S01", 60), pool("S02", 60)),
                List.of(pool("S01", 100)));
        assertEquals(0, plan.shortfall());
        assertEquals(100, totalQty(plan));
        assertTrue(plan.lines().stream().allMatch(l -> l.deliveryMeans() == DeliveryMeans.SHIP_DELIVERY));
        assertEquals(60, qtyFrom(plan, "S01", DeliveryMeans.SHIP_DELIVERY));
        assertEquals(40, qtyFrom(plan, "S02", DeliveryMeans.SHIP_DELIVERY));
    }

    @DisplayName("TC-BB-07: Ưu tiên Site tồn lớn nhất")
    @Test
    void tcBb07_prefersLargeStockSite() {
        var plan = MerchandiseAllocationEngine.allocate(100,
                List.of(pool("S01", 100), pool("S02", 60), pool("S03", 60)),
                List.of());
        assertEquals(0, plan.shortfall());
        assertEquals(1, plan.lines().size());
        assertLine(plan.lines().getFirst(), "S01", DeliveryMeans.SHIP_DELIVERY, 100);
    }

    @DisplayName("TC-BB-08: Tie tồn — ưu tiên ít Site")
    @Test
    void tcBb08_prefersFewerSitesOnStockTie() {
        var plan = MerchandiseAllocationEngine.allocate(100,
                List.of(pool("S01", 80), pool("S02", 80), pool("S03", 30)),
                List.of());
        assertEquals(0, plan.shortfall());
        assertEquals(100, totalQty(plan));
        assertEquals(2, plan.lines().size());
        assertEquals(80, qtyFrom(plan, "S01", DeliveryMeans.SHIP_DELIVERY));
        assertEquals(20, qtyFrom(plan, "S02", DeliveryMeans.SHIP_DELIVERY));
    }

    @DisplayName("TC-BB-09: Thiếu hàng ship only — lines rỗng")
    @Test
    void tcBb09_shortfallShipOnly_linesEmpty() {
        var plan = MerchandiseAllocationEngine.allocate(100,
                List.of(pool("S01", 30)), List.of());
        assertTrue(plan.lines().isEmpty());
        assertEquals(70, plan.shortfall());
        assertFalse(plan.hasNoEligibleSite());
    }

    @DisplayName("TC-BB-10: Thiếu hàng sau ship+air — lines rỗng")
    @Test
    void tcBb10_shortfallShipAndAir_linesEmpty() {
        var plan = MerchandiseAllocationEngine.allocate(300,
                List.of(pool("S01", 100)),
                List.of(pool("S02", 80)));
        assertTrue(plan.lines().isEmpty());
        assertEquals(200, plan.shortfall());
    }

    @DisplayName("TC-BB-11: Bay rỗng sau ship — lines rỗng")
    @Test
    void tcBb11_shortfallRemainingWithEmptyAir_linesEmpty() {
        var plan = MerchandiseAllocationEngine.allocate(200,
                List.of(pool("S01", 100), pool("S02", 50)),
                List.of());
        assertTrue(plan.lines().isEmpty());
        assertEquals(50, plan.shortfall());
    }

    @DisplayName("TC-BB-12: Biên Q=1, stock=1")
    @Test
    void tcBb12_boundaryQEqualsOneStockEqualsOne() {
        var plan = MerchandiseAllocationEngine.allocate(1,
                List.of(pool("S01", 1)), List.of());
        assertEquals(0, plan.shortfall());
        assertLine(plan.lines().getFirst(), "S01", DeliveryMeans.SHIP_DELIVERY, 1);
    }

    @DisplayName("TC-BB-13: Site stock=0 bị bỏ qua")
    @Test
    void tcBb13_siteWithZeroStockIsSkipped() {
        var plan = MerchandiseAllocationEngine.allocate(50,
                List.of(pool("S01", 0), pool("S02", 60)),
                List.of());
        assertEquals(0, plan.shortfall());
        assertEquals(1, plan.lines().size());
        assertLine(plan.lines().getFirst(), "S02", DeliveryMeans.SHIP_DELIVERY, 50);
    }

    @DisplayName("TC-BB-14: Q bằng đúng tổng tồn")
    @Test
    void tcBb14_quantityExactlyEqualsTotalStock() {
        var plan = MerchandiseAllocationEngine.allocate(160,
                List.of(pool("S01", 100), pool("S02", 60)),
                List.of());
        assertEquals(0, plan.shortfall());
        assertEquals(100, qtyFrom(plan, "S01", DeliveryMeans.SHIP_DELIVERY));
        assertEquals(60, qtyFrom(plan, "S02", DeliveryMeans.SHIP_DELIVERY));
    }

    @DisplayName("TC-BB-15: Bay thiếu một phần — lines rỗng")
    @Test
    void tcBb15_partialShortfallWhenAirInsufficient_linesEmpty() {
        var plan = MerchandiseAllocationEngine.allocate(250,
                List.of(pool("S01", 100)),
                List.of(pool("S02", 80)));
        assertTrue(plan.lines().isEmpty());
        assertEquals(150, plan.shortfall());
    }

    private static MerchandiseAllocationEngine.SitePool pool(String code, int stock) {
        return new MerchandiseAllocationEngine.SitePool(code, stock);
    }

    private static void assertLine(
            MerchandiseAllocationEngine.AllocationLine line,
            String site,
            DeliveryMeans means,
            int qty
    ) {
        assertEquals(site, line.siteCode());
        assertEquals(means, line.deliveryMeans());
        assertEquals(qty, line.quantity());
    }

    private static int totalQty(MerchandiseAllocationEngine.Plan plan) {
        return plan.lines().stream().mapToInt(MerchandiseAllocationEngine.AllocationLine::quantity).sum();
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

    /** Tổng SL theo Site (mọi phương thức) — dùng khi thứ tự dòng không quan trọng. */
    @SuppressWarnings("unused")
    private static Map<String, Integer> qtyBySite(MerchandiseAllocationEngine.Plan plan) {
        return plan.lines().stream()
                .collect(Collectors.groupingBy(
                        MerchandiseAllocationEngine.AllocationLine::siteCode,
                        Collectors.summingInt(MerchandiseAllocationEngine.AllocationLine::quantity)));
    }
}
