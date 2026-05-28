package com.orderingsystem.uc006;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.infrastructure.jpa.JpaBootstrap;
import com.orderingsystem.infrastructure.database.SiteMerchandiseRepository;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import com.orderingsystem.uc002.ImportRequestService;
import com.orderingsystem.uc002.boundary.dto.CreateImportRequestLineInput;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;
import com.orderingsystem.uc005.ImportRequestAcceptanceService;
import com.orderingsystem.uc006.boundary.dto.InventoryQueryDispatchResultDto;
import com.orderingsystem.uc009.SiteMerchandiseService;
import com.orderingsystem.uc010.SiteShippingService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryQueryServiceTest {

    private static final AuthService authService = new AuthService();
    private static final ImportRequestService importRequestService = new ImportRequestService();
    private static final ImportRequestAcceptanceService acceptanceService = new ImportRequestAcceptanceService();
    private static final SiteShippingService siteShippingService = new SiteShippingService();
    private static final SiteMerchandiseService siteMerchandiseService = new SiteMerchandiseService();
    private static final InventoryQueryService inventoryQueryService = new InventoryQueryService();
    private static final SiteMerchandiseRepository siteMerchandiseRepository = new SiteMerchandiseRepository();

    @BeforeAll
    static void setUp() {
        JpaBootstrap.init();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterEach
    void cleanup() {
        authService.logout();
        siteMerchandiseRepository.deleteAllBySiteCode(DatabaseSeeder.DEMO_SITE_CODE);
    }

    @AfterAll
    static void shutDown() {
        JpaBootstrap.shutdown();
    }

    @Test
    void dispatch_requiresOverseasAndProcessingStatus() {
        prepareSiteWithP001();
        String requestId = createAndAcceptRequest();

        authService.login("sales", "sales123");
        assertThrows(SecurityException.class, () -> inventoryQueryService.dispatchInventoryQueries(requestId));
        authService.logout();

        authService.login("overseas", "overseas123");
        InventoryQueryDispatchResultDto result = inventoryQueryService.dispatchInventoryQueries(requestId);
        assertEquals(1, result.totalQueries());
        assertEquals(1, result.pendingQueries());
        assertEquals(DatabaseSeeder.DEMO_SITE_CODE, result.siteGroups().getFirst().siteCode());
        assertEquals("P001", result.siteGroups().getFirst().lines().getFirst().merchandiseCode());
        assertTrue(result.merchandiseErrors().isEmpty());
        authService.logout();
    }

    @Test
    void dispatch_noSiteForMerchandise_recordsError() {
        authService.login("site01", "site123");
        siteShippingService.updateMyShipping(10, 3);
        authService.logout();

        authService.login("sales", "sales123");
        ImportRequestDto created = importRequestService.createImportRequest(List.of(
                new CreateImportRequestLineInput("P002", 50, "pcs", LocalDate.now().plusDays(20))));
        authService.logout();

        authService.login("overseas", "overseas123");
        acceptanceService.acceptRequest(created.requestId());
        InventoryQueryDispatchResultDto result =
                inventoryQueryService.dispatchInventoryQueries(created.requestId());
        assertEquals(0, result.totalQueries());
        assertEquals(1, result.merchandiseErrors().size());
        assertEquals("P002", result.merchandiseErrors().getFirst().merchandiseCode());
        authService.logout();
    }

    @Test
    void applyTimeout_marksPendingAsZero() {
        prepareSiteWithP001();
        String requestId = createAndAcceptRequest();

        authService.login("overseas", "overseas123");
        inventoryQueryService.dispatchInventoryQueries(requestId);
        assertEquals(1, inventoryQueryService.applyTimeoutAsZeroStock(requestId));
        InventoryQueryDispatchResultDto status = inventoryQueryService.getInventoryQueryStatus(requestId);
        assertTrue(status.allSitesResponded());
        assertEquals(0, status.siteGroups().getFirst().lines().getFirst().inStockQuantity());
        authService.logout();
    }

    private void prepareSiteWithP001() {
        authService.login("site01", "site123");
        siteShippingService.updateMyShipping(12, 4);
        siteMerchandiseService.addMerchandise("P001");
        authService.logout();
    }

    private String createAndAcceptRequest() {
        authService.login("sales", "sales123");
        ImportRequestDto created = importRequestService.createImportRequest(List.of(
                new CreateImportRequestLineInput("P001", 40, "box", LocalDate.now().plusDays(25))));
        authService.logout();

        authService.login("overseas", "overseas123");
        acceptanceService.acceptRequest(created.requestId());
        authService.logout();
        return created.requestId();
    }
}
