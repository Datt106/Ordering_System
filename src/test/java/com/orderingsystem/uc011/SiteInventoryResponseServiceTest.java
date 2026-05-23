package com.orderingsystem.uc011;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.domain.inventory.InventoryQuery;
import com.orderingsystem.infrastructure.jpa.JpaBootstrap;
import com.orderingsystem.infrastructure.repository.InventoryQueryRepository;
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
        JpaBootstrap.init();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterAll
    static void shutdown() {
        JpaBootstrap.shutdown();
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
