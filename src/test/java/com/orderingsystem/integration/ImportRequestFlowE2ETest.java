package com.orderingsystem.integration;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.core.domain.ShippingStatus;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.database.SiteMerchandiseRepository;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import com.orderingsystem.uc001.StandardMerchandiseCatalogService;
import com.orderingsystem.uc002.ImportRequestService;
import com.orderingsystem.uc002.boundary.dto.CreateImportRequestLineInput;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;
import com.orderingsystem.uc003.ImportRequestTrackingService;
import com.orderingsystem.uc003.boundary.dto.ImportRequestListItemDto;
import com.orderingsystem.uc003.boundary.dto.ImportRequestTrackingDetailDto;
import com.orderingsystem.uc005.ImportRequestAcceptanceService;
import com.orderingsystem.uc009.SiteMerchandiseService;
import com.orderingsystem.uc006.InventoryQueryService;
import com.orderingsystem.uc006.boundary.dto.InventoryQueryDispatchResultDto;
import com.orderingsystem.uc010.SiteShippingService;
import com.orderingsystem.uc004.boundary.dto.SiteDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Luồng E2E tối thiểu (service layer, DB thật SQLite):
 * seed → Site (ship/air + mặt hàng KD) → Sales tạo YC → Overseas tiếp nhận → Sales theo dõi.
 */
class ImportRequestFlowE2ETest {

    private static final AuthService authService = new AuthService();
    private static final StandardMerchandiseCatalogService catalogService =
            new StandardMerchandiseCatalogService();
    private static final SiteShippingService siteShippingService = new SiteShippingService();
    private static final SiteMerchandiseService siteMerchandiseService = new SiteMerchandiseService();
    private static final ImportRequestService importRequestService = new ImportRequestService();
    private static final ImportRequestAcceptanceService acceptanceService =
            new ImportRequestAcceptanceService();
    private static final ImportRequestTrackingService trackingService =
            new ImportRequestTrackingService();
    private static final InventoryQueryService inventoryQueryService = new InventoryQueryService();
    private static final SiteMerchandiseRepository siteMerchandiseRepository =
            new SiteMerchandiseRepository();

    @BeforeAll
    static void initDatabase() {
        DbManager.init("jdbc:sqlite:data/test-ordering.db", "", "");
        SchemaInitializer.apply();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterEach
    void cleanupSiteMerchandise() {
        authService.logout();
        siteMerchandiseRepository.deleteAllBySiteCode(DatabaseSeeder.DEMO_SITE_CODE);
    }

    @AfterAll
    static void shutdown() {
        DbManager.shutdown();
    }

    @Test
    @DisplayName("E2E: Site chuẩn bị → Sales tạo yêu cầu → Overseas tiếp nhận → Sales thấy Đang xử lý")
    void endToEnd_minimalImportFlow() {
        prepareDemoSite();

        ImportRequestDto created = salesCreatesImportRequest();
        String requestId = created.requestId();
        assertEquals(RequestStatus.CHO_XU_LY, created.status());

        overseasAccepts(requestId);

        InventoryQueryDispatchResultDto inventory = overseasDispatchesInventory(requestId);

        salesSeesProcessing(requestId);

        assertEquals(1, inventory.totalQueries());
        assertEquals(1, inventory.pendingQueries());
    }

    private InventoryQueryDispatchResultDto overseasDispatchesInventory(String requestId) {
        authService.login("overseas", "overseas123");
        InventoryQueryDispatchResultDto result = inventoryQueryService.dispatchInventoryQueries(requestId);
        authService.logout();
        return result;
    }

    private void prepareDemoSite() {
        siteMerchandiseRepository.deleteAllBySiteCode(DatabaseSeeder.DEMO_SITE_CODE);

        authService.login("site01", "site123");
        SiteDto site = siteShippingService.updateMyShipping(14, 5);
        assertEquals(DatabaseSeeder.DEMO_SITE_CODE, site.siteCode());
        assertEquals(ShippingStatus.DA_KHAI_BAO, site.shippingStatus());

        siteMerchandiseService.addMerchandise("P001");
        assertEquals(1, siteMerchandiseService.listMyMerchandise().size());
        authService.logout();

        authService.login("sales", "sales123");
        assertTrue(catalogService.listCatalogForBrowsing().stream()
                .anyMatch(d -> "P001".equals(d.merchandiseCode())
                        && d.merchandiseName() != null
                        && !d.merchandiseName().isBlank()));
        authService.logout();
    }

    private ImportRequestDto salesCreatesImportRequest() {
        authService.login("sales", "sales123");
        ImportRequestDto created = importRequestService.createImportRequest(List.of(
                new CreateImportRequestLineInput("P001", 80, "box", LocalDate.now().plusDays(21))));
        authService.logout();
        return created;
    }

    private void overseasAccepts(String requestId) {
        authService.login("overseas", "overseas123");
        assertTrue(acceptanceService.listPendingRequests().stream()
                .anyMatch(r -> requestId.equals(r.requestId())));

        ImportRequestDto accepted = acceptanceService.acceptRequest(requestId);
        assertEquals(RequestStatus.DANG_XU_LY, accepted.status());
        assertEquals("overseas", accepted.processedBy());
        assertTrue(accepted.processedAt() != null);

        assertFalse(acceptanceService.listPendingRequests().stream()
                .anyMatch(r -> requestId.equals(r.requestId())));
        authService.logout();
    }

    private void salesSeesProcessing(String requestId) {
        authService.login("sales", "sales123");

        ImportRequestListItemDto row = trackingService.listRequests().stream()
                .filter(r -> requestId.equals(r.requestId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Sales không thấy yêu cầu: " + requestId));
        assertEquals(RequestStatus.DANG_XU_LY, row.status());
        assertEquals(1, row.itemCount());

        ImportRequestTrackingDetailDto detail = trackingService.getRequestDetail(requestId).orElseThrow();
        assertEquals(RequestStatus.DANG_XU_LY, detail.request().status());
        assertEquals("P001", detail.request().items().getFirst().merchandiseCode());
        assertTrue(detail.childOrders().isEmpty());

        authService.logout();
    }
}
