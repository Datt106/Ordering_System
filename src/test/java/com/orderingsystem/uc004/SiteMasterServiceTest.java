package com.orderingsystem.uc004;

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

class SiteMasterServiceTest {

    private static final AuthService authService = new AuthService();
    private static final SiteMasterService siteMasterService = new SiteMasterService();

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
    void registerAndUpdateMaster_requiresOverseas() {
        authService.login("sales", "sales123");
        assertThrows(SecurityException.class, () ->
                siteMasterService.registerSite("X-NEW", "New Site", null));

        authService.login("overseas", "overseas123");
        SiteDto created = siteMasterService.registerSite("X-NEW", "New Site", "note");
        assertEquals("X-NEW", created.siteCode());
        assertEquals(ShippingStatus.CHUA_KHAI_BAO, created.shippingStatus());

        SiteDto updated = siteMasterService.updateMaster("X-NEW", "Renamed", "other");
        assertEquals("Renamed", updated.siteName());
        assertEquals("other", updated.otherInfo());

        siteMasterService.deleteSite("X-NEW");
    }

    @Test
    void register_duplicateCode_fails() {
        authService.login("overseas", "overseas123");
        assertThrows(IllegalArgumentException.class, () ->
                siteMasterService.registerSite(DatabaseSeeder.DEMO_SITE_CODE, "Dup", null));
    }
}
