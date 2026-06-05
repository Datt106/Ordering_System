package com.orderingsystem.integration;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.InventoryQueryRepository;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.database.SiteMerchandiseRepository;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import com.orderingsystem.uc002.ImportRequestService;
import com.orderingsystem.uc002.boundary.dto.CreateImportRequestLineInput;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;
import com.orderingsystem.uc005.ImportRequestAcceptanceService;
import com.orderingsystem.uc006.InventoryQueryService;
import com.orderingsystem.uc007.OrderSplitService;
import com.orderingsystem.uc008.OrderDispatchService;
import com.orderingsystem.uc009.SiteMerchandiseService;
import com.orderingsystem.uc010.SiteShippingService;
import com.orderingsystem.uc012.SiteOrderConfirmService;
import com.orderingsystem.uc013.controller.WarehouseOrderViewController;
import com.orderingsystem.uc014.controller.WarehouseReconcileController;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation")
class WarehouseManagementFlowTest {

    private static final AuthService authService = new AuthService();
    private static final ImportRequestService importRequestService = new ImportRequestService();
    private static final ImportRequestAcceptanceService acceptanceService = new ImportRequestAcceptanceService();
    private static final InventoryQueryService inventoryQueryService = new InventoryQueryService();
    private static final InventoryQueryRepository inventoryQueryRepository = new InventoryQueryRepository();
    private static final OrderSplitService orderSplitService = new OrderSplitService();
    private static final OrderDispatchService orderDispatchService = new OrderDispatchService();
    private static final SiteOrderConfirmService siteOrderConfirmService = new SiteOrderConfirmService();
    
    // Đã đổi sang Controller
    private static final WarehouseOrderViewController warehouseOrderViewController = new WarehouseOrderViewController();
    private static final WarehouseReconcileController warehouseReconcileController = new WarehouseReconcileController();
    
    private static final PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepository();
    private static final SiteShippingService siteShippingService = new SiteShippingService();
    private static final SiteMerchandiseService siteMerchandiseService = new SiteMerchandiseService();
    private static final SiteMerchandiseRepository siteMerchandiseRepository = new SiteMerchandiseRepository();

    @BeforeAll
    static void initDatabase() {
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
    static void shutdown() {
        DbManager.shutdown();
    }

    @Test
    @DisplayName("Kho: danh sách đơn Đã xác nhận → đối chiếu khớp → Đã nhập kho")
    void warehouseListsAndReconcilesMatchingQuantity() {
        String orderId = prepareConfirmedOrder(40);

        authService.login("warehouse", "wh123");

        assertTrue(warehouseOrderViewController.listOrders(null, null, null).stream()
                .anyMatch(o -> orderId.equals(o.orderId())));

        var filtered = warehouseOrderViewController.listOrders(
                OrderStatus.DA_XAC_NHAN, DatabaseSeeder.DEMO_SITE_CODE, "P001");
        assertTrue(filtered.stream().anyMatch(o -> orderId.equals(o.orderId())));
        assertEquals(40, filtered.stream()
                .filter(o -> orderId.equals(o.orderId()))
                .findFirst()
                .orElseThrow()
                .quantityOrdered());

        // Đã sửa: Truyền thêm Instant.now()
        var result = warehouseReconcileController.recordInbound(orderId, 40, Instant.now());
        assertEquals(OrderStatus.DA_NHAP_KHO, result.status());
        assertEquals(0, result.quantityDiff());

        assertTrue(warehouseOrderViewController.listOrders(OrderStatus.DA_NHAP_KHO, null, null).stream()
                .anyMatch(o -> orderId.equals(o.orderId())));

        authService.logout();
    }

    @Test
    @DisplayName("Kho: đối chiếu lệch số lượng → Sai lệch")
    void warehouseReconcile_recordsDiscrepancy() {
        String orderId = prepareConfirmedOrder(30);

        authService.login("warehouse", "wh123");
        // Đã sửa: Truyền thêm Instant.now()
        var result = warehouseReconcileController.recordInbound(orderId, 25, Instant.now());
        assertEquals(OrderStatus.SAI_LECH, result.status());
        assertEquals(-5, result.quantityDiff());
        assertEquals(OrderStatus.SAI_LECH, purchaseOrderRepository.findById(orderId).orElseThrow().getStatus());
        authService.logout();
    }

    private String prepareConfirmedOrder(int quantity) {
        authService.login("site01", "site123");
        siteShippingService.updateMyShipping(8, 3);
        siteMerchandiseService.addMerchandise("P001");
        authService.logout();

        authService.login("sales", "sales123");
        ImportRequestDto created = importRequestService.createImportRequest(List.of(
                new CreateImportRequestLineInput("P001", quantity, "unit", LocalDate.now().plusDays(18))));
        authService.logout();

        authService.login("overseas", "overseas123");
        String requestId = created.requestId();
        acceptanceService.acceptRequest(requestId);
        inventoryQueryService.dispatchInventoryQueries(requestId);
        inventoryQueryRepository.findByRequestId(requestId).forEach(q -> {
            q.setInStockQuantity(100);
            q.setRespondedAt(Instant.now());
            inventoryQueryRepository.save(q);
        });
        orderSplitService.confirmSplit(requestId, LocalDate.now());
        orderDispatchService.dispatchOrders(requestId);
        authService.logout();

        authService.login("site01", "site123");
        String orderId = siteOrderConfirmService.listMyIncomingOrders().stream()
                .filter(o -> requestId.equals(o.requestId()))
                .findFirst()
                .orElseThrow()
                .orderId();
        siteOrderConfirmService.confirmOrder(orderId);
        authService.logout();

        return orderId;
    }
}