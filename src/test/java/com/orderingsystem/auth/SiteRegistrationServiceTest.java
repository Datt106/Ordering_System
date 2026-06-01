package com.orderingsystem.auth;

import com.orderingsystem.core.domain.User;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.core.domain.Site;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.database.SiteRepository;
import com.orderingsystem.infrastructure.database.UserRepository;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiteRegistrationServiceTest {

    private static final SiteRegistrationService registrationService = new SiteRegistrationService();
    private static final SiteRepository siteRepository = new SiteRepository();
    private static final UserRepository userRepository = new UserRepository();

    @BeforeAll
    static void initDatabase() {
        DbManager.init("jdbc:sqlite:data/test-ordering.db", "", "");
        SchemaInitializer.apply();
        new DatabaseSeeder().seedDemoData();
        if (!siteRepository.existsByCode(DatabaseSeeder.DEMO_SITE_CODE_2)) {
            siteRepository.save(new Site(DatabaseSeeder.DEMO_SITE_CODE_2, "Demo Import Site Osaka", "Test"));
        }
        userRepository.deleteBySiteCode(DatabaseSeeder.DEMO_SITE_CODE_2);
    }

    @AfterAll
    static void shutdown() {
        DbManager.shutdown();
    }

    @Test
    void listRegistrableSites_includesS02_notS01() {
        var codes = registrationService.listRegistrableSites().stream()
                .map(s -> s.siteCode())
                .toList();
        assertFalse(codes.contains(DatabaseSeeder.DEMO_SITE_CODE));
        assertTrue(codes.contains(DatabaseSeeder.DEMO_SITE_CODE_2));
    }

    @Test
    void registerSiteAccount_success() {
        if (!siteRepository.existsByCode("S99")) {
            siteRepository.save(new Site("S99", "Test Site", null));
        }
        userRepository.deleteBySiteCode("S99");

        User user = registrationService.registerSiteAccount("S99", "site_s99", "secret99");
        assertEquals(UserRole.SITE, user.getRole());
        assertEquals("S99", user.getSiteCode());
        assertTrue(userRepository.existsByUsername("site_s99"));
    }

    @Test
    void registerSiteAccount_duplicateSite_fails() {
        assertThrows(IllegalStateException.class, () ->
                registrationService.registerSiteAccount(DatabaseSeeder.DEMO_SITE_CODE, "other", "pass1234"));
    }

    @Test
    void registerSiteAccount_unknownSite_fails() {
        assertThrows(IllegalArgumentException.class, () ->
                registrationService.registerSiteAccount("NOPE", "x", "pass1234"));
    }
}
