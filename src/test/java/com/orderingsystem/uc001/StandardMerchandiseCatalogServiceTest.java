package com.orderingsystem.uc001;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.database.MerchandiseCatalogRepository;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import com.orderingsystem.uc001.boundary.dto.StandardMerchandiseDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardMerchandiseCatalogServiceTest {

    private static final AuthService authService = new AuthService();
    private static final StandardMerchandiseCatalogService catalogService =
            new StandardMerchandiseCatalogService();
    private static final MerchandiseCatalogRepository catalogRepository = new MerchandiseCatalogRepository();

    @BeforeAll
    static void setUp() {
        DbManager.init("jdbc:sqlite:data/test-ordering.db", "", "");
        SchemaInitializer.apply();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterEach
    void logout() {
        authService.logout();
        if (catalogRepository.existsByCode("X-CAT")) {
            if (!catalogRepository.isReferenced("X-CAT")) {
                catalogRepository.deleteByCode("X-CAT");
            }
        }
    }

    @AfterAll
    static void shutDown() {
        DbManager.shutdown();
    }

    @Test
    void catalog_write_requiresSales() {
        authService.login("overseas", "overseas123");
        assertThrows(SecurityException.class, catalogService::listAll);
        assertThrows(SecurityException.class, () ->
                catalogService.registerMerchandise("X1", "Name", null));
    }

    @Test
    void site_canBrowseCatalogWithDescriptions() {
        authService.login("site01", "site123");
        StandardMerchandiseDto p001 = catalogService.listCatalogForBrowsing().stream()
                .filter(d -> "P001".equals(d.merchandiseCode()))
                .findFirst()
                .orElseThrow();
        assertEquals("Precision Bearing", p001.merchandiseName());
        assertTrue(p001.description().contains("bearing"));
    }

    @Test
    void registerListDelete_success() {
        authService.login("sales", "sales123");

        StandardMerchandiseDto created = catalogService.registerMerchandise(
                "X-CAT", "Test Item", "Demo description");
        assertEquals("X-CAT", created.merchandiseCode());
        assertEquals("Test Item", created.merchandiseName());

        catalogService.deleteMerchandise("X-CAT");
        assertTrue(catalogService.listAll().stream().noneMatch(d -> "X-CAT".equals(d.merchandiseCode())));
    }

    @Test
    void register_duplicate_fails() {
        authService.login("sales", "sales123");
        assertThrows(IllegalArgumentException.class, () ->
                catalogService.registerMerchandise("P001", "Dup", null));
    }

    @Test
    void delete_referenced_fails() {
        authService.login("sales", "sales123");
        assertThrows(IllegalStateException.class, () -> catalogService.deleteMerchandise("P001"));
    }
}
