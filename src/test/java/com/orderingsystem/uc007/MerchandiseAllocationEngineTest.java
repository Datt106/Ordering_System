package com.orderingsystem.uc007;

import com.orderingsystem.core.domain.DeliveryMeans;
import com.orderingsystem.uc007.controller.MerchandiseAllocationEngine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MerchandiseAllocationEngineTest {

    @Test
    void singleSiteShipCoversAll() {
        var plan = MerchandiseAllocationEngine.allocate(
                150,
                List.of(new MerchandiseAllocationEngine.SitePool("S01", 150)),
                List.of()
        );
        assertTrue(plan.lines().stream().allMatch(l -> l.deliveryMeans() == DeliveryMeans.SHIP_DELIVERY));
        assertEquals(150, plan.lines().stream().mapToInt(MerchandiseAllocationEngine.AllocationLine::quantity).sum());
        assertEquals(1, plan.lines().size());
    }

    @Test
    void shipThenAir_minSites() {
        var plan = MerchandiseAllocationEngine.allocate(
                250,
                List.of(new MerchandiseAllocationEngine.SitePool("S01", 100)),
                List.of(new MerchandiseAllocationEngine.SitePool("S03", 200))
        );
        assertEquals(250, plan.lines().stream().mapToInt(MerchandiseAllocationEngine.AllocationLine::quantity).sum());
        assertEquals(100, plan.lines().stream()
                .filter(l -> l.siteCode().equals("S01"))
                .mapToInt(MerchandiseAllocationEngine.AllocationLine::quantity)
                .sum());
        assertEquals(150, plan.lines().stream()
                .filter(l -> l.siteCode().equals("S03"))
                .mapToInt(MerchandiseAllocationEngine.AllocationLine::quantity)
                .sum());
    }

    @Test
    void prefersShipOverSingleSiteAir() {
        var plan = MerchandiseAllocationEngine.allocate(
                100,
                List.of(
                        new MerchandiseAllocationEngine.SitePool("S2", 60),
                        new MerchandiseAllocationEngine.SitePool("S3", 60)
                ),
                List.of(new MerchandiseAllocationEngine.SitePool("S1", 100))
        );
        int shipQty = plan.lines().stream()
                .filter(l -> l.deliveryMeans() == DeliveryMeans.SHIP_DELIVERY)
                .mapToInt(MerchandiseAllocationEngine.AllocationLine::quantity)
                .sum();
        assertEquals(100, shipQty);
        assertEquals(2, plan.lines().stream()
                .map(MerchandiseAllocationEngine.AllocationLine::siteCode)
                .distinct()
                .count());
    }

    @Test
    void prefersLargeStockSiteOverMoreSmallSites() {
        var plan = MerchandiseAllocationEngine.allocate(
                100,
                List.of(
                        new MerchandiseAllocationEngine.SitePool("S1", 100),
                        new MerchandiseAllocationEngine.SitePool("S2", 60),
                        new MerchandiseAllocationEngine.SitePool("S3", 60)
                ),
                List.of()
        );
        assertEquals(100, plan.lines().stream().mapToInt(MerchandiseAllocationEngine.AllocationLine::quantity).sum());
        assertEquals(1, plan.lines().size());
        assertEquals("S1", plan.lines().get(0).siteCode());
    }

    @Test
    void whenSameStockLevel_prefersFewerSites() {
        var plan = MerchandiseAllocationEngine.allocate(
                100,
                List.of(
                        new MerchandiseAllocationEngine.SitePool("S1", 80),
                        new MerchandiseAllocationEngine.SitePool("S2", 80),
                        new MerchandiseAllocationEngine.SitePool("S3", 30)
                ),
                List.of()
        );
        assertEquals(100, plan.lines().stream().mapToInt(MerchandiseAllocationEngine.AllocationLine::quantity).sum());
        assertEquals(2, plan.lines().size());
        assertTrue(plan.lines().stream().anyMatch(l -> l.siteCode().equals("S1")));
        assertTrue(plan.lines().stream().anyMatch(l -> l.siteCode().equals("S2")));
    }

    @Test
    void shortfallWhenInsufficientStock() {
        var plan = MerchandiseAllocationEngine.allocate(
                100,
                List.of(new MerchandiseAllocationEngine.SitePool("S1", 30)),
                List.of()
        );
        assertEquals(70, plan.shortfall());
    }
}
