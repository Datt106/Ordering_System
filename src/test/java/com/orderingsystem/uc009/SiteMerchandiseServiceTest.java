package com.orderingsystem.uc009;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.database.SiteMerchandiseRepository;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import com.orderingsystem.uc009.boundary.dto.SiteMerchandiseDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiteMerchandiseServiceTest {

    private static final AuthService authService = new AuthService();
    private static final SiteMerchandiseService merchandiseService = new SiteMerchandiseService();
    private static final SiteMerchandiseRepository siteMerchandiseRepository = new SiteMerchandiseRepository();

    @BeforeAll
    static void setUp() {
        DbManager.init("jdbc:sqlite:data/test-ordering.db", "", "");
        SchemaInitializer.apply();
        new DatabaseSeeder().seedDemoData();
    }

    @BeforeEach
    @AfterEach
    void resetSiteMerchandise() {
        authService.logout();
        siteMerchandiseRepository.deleteAllBySiteCode(DatabaseSeeder.DEMO_SITE_CODE);
    }

    @AfterAll
    static void shutDown() {
        DbManager.shutdown();
    }

    @Test
    void addListRemove_requiresSiteRole() {
        authService.login("overseas", "overseas123");
        assertThrows(SecurityException.class, () -> merchandiseService.listMyMerchandise());
        assertThrows(SecurityException.class, () -> merchandiseService.addMerchandise("P001"));
    }

    @Test
    void addListRemove_success() {
        authService.login("site01", "site123");

        assertTrue(merchandiseService.listMyMerchandise().isEmpty());

        SiteMerchandiseDto p1 = merchandiseService.addMerchandise("P001");
        assertEquals(DatabaseSeeder.DEMO_SITE_CODE, p1.siteCode());
        assertEquals("P001", p1.merchandiseCode());

        merchandiseService.addMerchandise("P002");
        assertEquals(2, merchandiseService.listMyMerchandise().size());

        merchandiseService.removeMerchandise("P001");
        assertEquals(1, merchandiseService.listMyMerchandise().size());
        assertEquals("P002", merchandiseService.listMyMerchandise().getFirst().merchandiseCode());
    }

    @Test
    void add_duplicate_fails() {
        authService.login("site01", "site123");
        merchandiseService.addMerchandise("P003");
        assertThrows(IllegalArgumentException.class, () -> merchandiseService.addMerchandise("P003"));
    }

    @Test
    void add_unknownCatalog_fails() {
        authService.login("site01", "site123");
        assertThrows(IllegalArgumentException.class, () -> merchandiseService.addMerchandise("NO-SUCH"));
    }

    @Test
    void remove_notListed_fails() {
        authService.login("site01", "site123");
        assertThrows(IllegalArgumentException.class, () -> merchandiseService.removeMerchandise("P001"));
    }
}
