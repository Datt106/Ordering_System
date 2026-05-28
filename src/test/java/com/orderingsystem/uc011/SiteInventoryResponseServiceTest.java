package com.orderingsystem.uc011;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.InventoryQuery;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.database.InventoryQueryRepository;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SiteInventoryResponseServiceTest {

    private static final AuthService authService = new AuthService();
    private static final SiteInventoryResponseService service = new SiteInventoryResponseService();
    private static final InventoryQueryRepository queryRepository = new InventoryQueryRepository();

    @BeforeAll
    static void initDb() {
        DbManager.init("jdbc:sqlite:data/test-ordering.db", "", "");
        SchemaInitializer.apply();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterAll
    static void shutdown() {
        DbManager.shutdown();
    }

    @Test
    void siteRespondsToPendingQuery() {
        authService.login("site01", "site123");
        try {
            queryRepository.deleteByRequestId("REQ-TEST");
            String queryId = "IQ-TEST-S01-P001";
            queryRepository.save(new InventoryQuery(
                    queryId, "REQ-TEST", DatabaseSeeder.DEMO_SITE_CODE,
                    "P001", 0, "pcs", null));

            assertFalse(service.listMyPendingQueries().isEmpty());
            var responded = service.respond(queryId, 42);
            assertEquals(42, responded.inStockQuantity());
            assertFalse(responded.pending());
        } finally {
            authService.logout();
            queryRepository.deleteByRequestId("REQ-TEST");
        }
    }
}
