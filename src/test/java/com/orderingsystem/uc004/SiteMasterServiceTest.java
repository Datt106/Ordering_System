package com.orderingsystem.uc004;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.SiteRegistrationService;
import com.orderingsystem.core.domain.ShippingStatus;
import com.orderingsystem.core.domain.Site;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.database.SiteRepository;
import com.orderingsystem.infrastructure.database.UserRepository;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import com.orderingsystem.uc004.boundary.dto.SiteDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiteMasterServiceTest {

    private static final AuthService authService = new AuthService();
    private static final SiteMasterService siteMasterService = new SiteMasterService();
    private static final SiteRepository siteRepository = new SiteRepository();
    private static final UserRepository userRepository = new UserRepository();
    private static final SiteRegistrationService siteRegistrationService = new SiteRegistrationService();

    @BeforeAll
    static void setUp() {
        DbManager.init("jdbc:sqlite:data/test-ordering.db", "", "");
        SchemaInitializer.apply();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterEach
    void logout() {
        authService.logout();
    }

    @AfterAll
    static void shutDown() {
        DbManager.shutdown();
    }

    @Test
    void registerAndUpdateMaster_requiresOverseas() {
        String code = "X-NEW";
        authService.login("overseas", "overseas123");
        if (siteRepository.existsByCode(code)) {
            userRepository.deleteBySiteCode(code);
            if (siteRepository.findByCode(code).orElseThrow().isActive()) {
                siteMasterService.deactivateSite(code);
            }
            siteMasterService.deleteSite(code);
        }

        authService.logout();
        authService.login("sales", "sales123");
        assertThrows(SecurityException.class, () ->
                siteMasterService.registerSite(code, "New Site", null));

        authService.login("overseas", "overseas123");
        SiteDto created = siteMasterService.registerSite(code, "New Site", "note");
        assertEquals("X-NEW", created.siteCode());
        assertEquals(ShippingStatus.CHUA_KHAI_BAO, created.shippingStatus());

        SiteDto updated = siteMasterService.updateMaster("X-NEW", "Renamed", "other");
        assertEquals("Renamed", updated.siteName());
        assertEquals("other", updated.otherInfo());

        siteMasterService.deactivateSite("X-NEW");
        siteMasterService.deleteSite("X-NEW");
    }

    @Test
    void register_duplicateCode_fails() {
        authService.login("overseas", "overseas123");
        assertThrows(IllegalArgumentException.class, () ->
                siteMasterService.registerSite(DatabaseSeeder.DEMO_SITE_CODE, "Dup", null));
    }

    @Test
    void deactivate_activate_andDeleteInactive() {
        authService.login("overseas", "overseas123");
        String code = "X-LIFE";
        if (!siteRepository.existsByCode(code)) {
            siteMasterService.registerSite(code, "Lifecycle Site", null);
        }
        userRepository.deleteBySiteCode(code);

        siteRegistrationService.registerSiteAccount(code, "site_xlife", "pass1234");
        SiteDto deactivated = siteMasterService.deactivateSite(code);
        assertFalse(deactivated.active());

        authService.logout();
        assertThrows(IllegalStateException.class, () -> authService.login("site_xlife", "pass1234"));

        authService.login("overseas", "overseas123");
        SiteDto reactivated = siteMasterService.activateSite(code);
        assertTrue(reactivated.active());

        siteMasterService.deactivateSite(code);
        siteMasterService.deleteSite(code);
        assertFalse(siteRepository.existsByCode(code));
        assertFalse(userRepository.existsByUsername("site_xlife"));
    }

    @Test
    void delete_activeSite_fails() {
        authService.login("overseas", "overseas123");
        assertThrows(IllegalStateException.class, () -> siteMasterService.deleteSite(DatabaseSeeder.DEMO_SITE_CODE));
    }

    @Test
    void register_afterHardDelete_allowsSameCode() {
        authService.login("overseas", "overseas123");
        String code = "X-DEL";
        userRepository.deleteBySiteCode(code);
        if (siteRepository.existsByCode(code)) {
            siteRepository.setActive(code, false);
            siteMasterService.deleteSite(code);
        }
        SiteDto created = siteMasterService.registerSite(code, "Recreated", null);
        assertEquals(code, created.siteCode());
        siteMasterService.deactivateSite(code);
        siteMasterService.deleteSite(code);
    }
}
