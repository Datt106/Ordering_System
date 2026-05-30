package com.orderingsystem.uc007;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.InventoryQuery;
import com.orderingsystem.core.domain.DeliveryMeans;
import com.orderingsystem.core.domain.ItemStatus;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.database.InventoryQueryRepository;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.infrastructure.database.SiteMerchandiseRepository;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import com.orderingsystem.uc002.ImportRequestService;
import com.orderingsystem.uc002.boundary.dto.CreateImportRequestLineInput;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;
import com.orderingsystem.uc005.ImportRequestAcceptanceService;
import com.orderingsystem.uc006.InventoryQueryService;
import com.orderingsystem.uc009.SiteMerchandiseService;
import com.orderingsystem.uc010.SiteShippingService;
import com.orderingsystem.uc007.boundary.dto.ManualSplitLineInput;
import com.orderingsystem.uc007.boundary.dto.ManualSplitValidationResultDto;
import com.orderingsystem.uc007.boundary.dto.OrderSplitResultDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderSplitServiceTest {

    private static final AuthService authService = new AuthService();
    private static final ImportRequestService importRequestService = new ImportRequestService();
    private static final ImportRequestAcceptanceService acceptanceService = new ImportRequestAcceptanceService();
    private static final InventoryQueryService inventoryQueryService = new InventoryQueryService();
    private static final SiteShippingService siteShippingService = new SiteShippingService();
    private static final SiteMerchandiseService siteMerchandiseService = new SiteMerchandiseService();
    private static final OrderSplitService orderSplitService = new OrderSplitService();
    private static final InventoryQueryRepository inventoryQueryRepository = new InventoryQueryRepository();
    private static final PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepository();
    private static final SiteMerchandiseRepository siteMerchandiseRepository = new SiteMerchandiseRepository();

    @BeforeAll
    static void initDatabase() {
        DbManager.init("jdbc:sqlite:data/test-ordering.db", "", "");
        SchemaInitializer.apply();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterAll
    static void shutdown() {
        DbManager.shutdown();
    }

    @BeforeEach
    void loginOverseas() {
        authService.logout();
        siteMerchandiseRepository.deleteAllBySiteCode(DatabaseSeeder.DEMO_SITE_CODE);
        authService.login("overseas", "overseas123");
    }

    @Test
    void preview_requiresOverseasRole() {
        authService.logout();
        authService.login("sales", "sales123");
        assertThrows(SecurityException.class, () ->
                orderSplitService.previewSplit("REQ-X", LocalDate.now()));
    }

    @Test
    void confirmSplit_afterInventoryResponse_createsPurchaseOrders() {
        String requestId = prepareRequestWithStock(80);

        LocalDate start = LocalDate.now();
        OrderSplitResultDto preview = orderSplitService.previewSplit(requestId, start);
        assertTrue(preview.readyToConfirm());
        assertEquals(1, preview.plans().size());
        assertTrue(preview.allMerchandiseSucceeded());
        assertEquals(1, preview.allLines().size());
        assertEquals(DeliveryMeans.SHIP_DELIVERY, preview.allLines().getFirst().deliveryMeans());

        OrderSplitResultDto confirmed = orderSplitService.confirmSplit(requestId, start);
        assertEquals(1, confirmed.allLines().size());
        assertEquals(1, purchaseOrderRepository.findByRequestId(requestId).size());
        assertEquals(80, purchaseOrderRepository.findByRequestId(requestId).getFirst().getQuantityOrdered());
    }

    @Test
    void validateManualSplit_acceptsMatchingPlan() {
        String requestId = prepareRequestWithStock(80);
        LocalDate start = LocalDate.now();
        ManualSplitValidationResultDto result = orderSplitService.validateManualSplit(
                requestId,
                start,
                List.of(new ManualSplitLineInput(DatabaseSeeder.DEMO_SITE_CODE, "P001", 80, DeliveryMeans.SHIP_DELIVERY))
        );
        assertTrue(result.valid());
        assertEquals(1, result.preview().allLines().size());
    }

    @Test
    void validateManualSplit_rejectsWrongQuantity() {
        String requestId = prepareRequestWithStock(80);
        ManualSplitValidationResultDto result = orderSplitService.validateManualSplit(
                requestId,
                LocalDate.now(),
                List.of(new ManualSplitLineInput(DatabaseSeeder.DEMO_SITE_CODE, "P001", 79, DeliveryMeans.SHIP_DELIVERY))
        );
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("tổng phân bổ")));
    }

    @Test
    void confirmManualSplit_persistsPurchaseOrders() {
        String requestId = prepareRequestWithStock(80);
        LocalDate start = LocalDate.now();
        OrderSplitResultDto confirmed = orderSplitService.confirmManualSplit(
                requestId,
                start,
                List.of(new ManualSplitLineInput(DatabaseSeeder.DEMO_SITE_CODE, "P001", 80, DeliveryMeans.SHIP_DELIVERY))
        );
        assertEquals(1, confirmed.allLines().size());
        assertEquals(1, purchaseOrderRepository.findByRequestId(requestId).size());
    }

    @Test
    void aggregatesSameMerchandiseLines() {
        authService.logout();
        authService.login("sales", "sales123");
        ImportRequestDto created = importRequestService.createImportRequest(List.of(
                new CreateImportRequestLineInput("P001", 40, "unit", LocalDate.now().plusDays(20)),
                new CreateImportRequestLineInput("P001", 10, "unit", LocalDate.now().plusDays(15))
        ));
        authService.logout();
        authService.login("overseas", "overseas123");

        String requestId = created.requestId();
        acceptanceService.acceptRequest(requestId);
        inventoryQueryService.dispatchInventoryQueries(requestId);
        respondAllStock(requestId, 100);

        OrderSplitResultDto preview = orderSplitService.previewSplit(requestId, LocalDate.now());
        assertEquals(50, preview.plans().getFirst().quantityNeeded());
        assertEquals(LocalDate.now().plusDays(15), preview.plans().getFirst().targetDeliveryDate());
    }

    private String prepareRequestWithStock(int stock) {
        authService.logout();
        authService.login("site01", "site123");
        siteShippingService.updateMyShipping(10, 3);
        siteMerchandiseService.addMerchandise("P001");
        authService.logout();

        authService.login("sales", "sales123");
        ImportRequestDto created = importRequestService.createImportRequest(List.of(
                new CreateImportRequestLineInput("P001", 80, "unit", LocalDate.now().plusDays(20))));
        authService.logout();

        authService.login("overseas", "overseas123");
        String requestId = created.requestId();
        acceptanceService.acceptRequest(requestId);
        inventoryQueryService.dispatchInventoryQueries(requestId);
        respondAllStock(requestId, stock);
        return requestId;
    }

    private void respondAllStock(String requestId, int stock) {
        for (InventoryQuery query : inventoryQueryRepository.findByRequestId(requestId)) {
            query.setInStockQuantity(stock);
            query.setRespondedAt(Instant.now());
            inventoryQueryRepository.save(query);
        }
    }
}
