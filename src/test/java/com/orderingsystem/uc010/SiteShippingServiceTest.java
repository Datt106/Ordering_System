package com.orderingsystem.uc010;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.ShippingStatus;
import com.orderingsystem.infrastructure.jpa.JpaBootstrap;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import com.orderingsystem.uc004.boundary.dto.SiteDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SiteShippingServiceTest {

    private static final AuthService authService = new AuthService();
    private static final SiteShippingService shippingService = new SiteShippingService();

    @BeforeAll
    static void setUp() {
        JpaBootstrap.init();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterEach
    void logout() {
        authService.logout();
    }

    @AfterAll
    static void shutDown() {
        JpaBootstrap.shutdown();
    }

    @Test
    void updateMyShipping_requiresSiteRole() {
        authService.login("overseas", "overseas123");
        assertThrows(SecurityException.class, () -> shippingService.updateMyShipping(10, 3));
    }

    @Test
    void siteUser_updatesOwnSite() {
        authService.login("site01", "site123");
        SiteDto dto = shippingService.updateMyShipping(12, 4);
        assertEquals(DatabaseSeeder.DEMO_SITE_CODE, dto.siteCode());
        assertEquals(12, dto.shipDays());
        assertEquals(4, dto.airDays());
        assertEquals(ShippingStatus.DA_KHAI_BAO, dto.shippingStatus());

        SiteDto read = shippingService.getMySite();
        assertEquals(12, read.shipDays());
    }

    @Test
    void invalidDays_throws() {
        authService.login("site01", "site123");
        assertThrows(IllegalArgumentException.class, () -> shippingService.updateMyShipping(0, 5));
    }
}
