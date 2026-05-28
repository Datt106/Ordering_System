package com.orderingsystem.uc012;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.InventoryQueryRepository;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import com.orderingsystem.uc002.ImportRequestService;
import com.orderingsystem.uc002.boundary.dto.CreateImportRequestLineInput;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;
import com.orderingsystem.uc005.ImportRequestAcceptanceService;
import com.orderingsystem.uc006.InventoryQueryService;
import com.orderingsystem.uc007.OrderSplitService;
import com.orderingsystem.uc008.OrderDispatchService;
import com.orderingsystem.uc010.SiteShippingService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiteOrderConfirmServiceTest {
    private static final AuthService authService = new AuthService();
    private static final ImportRequestService importRequestService = new ImportRequestService();
    private static final ImportRequestAcceptanceService acceptanceService = new ImportRequestAcceptanceService();
    private static final InventoryQueryService inventoryQueryService = new InventoryQueryService();
    private static final InventoryQueryRepository inventoryQueryRepository = new InventoryQueryRepository();
    private static final OrderSplitService orderSplitService = new OrderSplitService();
    private static final OrderDispatchService orderDispatchService = new OrderDispatchService();
    private static final SiteOrderConfirmService siteOrderConfirmService = new SiteOrderConfirmService();
    private static final PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepository();
    private static final SiteShippingService siteShippingService = new SiteShippingService();

    @BeforeAll
    static void init() {
        DbManager.init("jdbc:sqlite:data/test-ordering.db", "", "");
        SchemaInitializer.apply();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterAll
    static void shutdown() {
        DbManager.shutdown();
    }

    @Test
    void siteCanConfirmIncomingOrder() {
        String requestId = prepareDispatchedOrder();

        authService.logout();
        authService.login("site01", "site123");
        var incoming = siteOrderConfirmService.listMyIncomingOrders();
        assertTrue(incoming.stream().anyMatch(o -> o.requestId().equals(requestId)));

        String orderId = incoming.stream().filter(o -> o.requestId().equals(requestId)).findFirst().orElseThrow().orderId();
        var confirmed = siteOrderConfirmService.confirmOrder(orderId);
        assertEquals(OrderStatus.DA_XAC_NHAN, confirmed.status());
        assertEquals(OrderStatus.DA_XAC_NHAN, purchaseOrderRepository.findById(orderId).orElseThrow().getStatus());
    }

    @Test
    void siteConfirm_requiresSiteRole() {
        authService.logout();
        authService.login("overseas", "overseas123");
        assertThrows(SecurityException.class, siteOrderConfirmService::listMyIncomingOrders);
    }

    private String prepareDispatchedOrder() {
        authService.logout();
        authService.login("site01", "site123");
        siteShippingService.updateMyShipping(7, 2);
        authService.logout();

        authService.login("sales", "sales123");
        ImportRequestDto created = importRequestService.createImportRequest(List.of(
                new CreateImportRequestLineInput("P001", 30, "unit", LocalDate.now().plusDays(15))));
        authService.logout();

        authService.login("overseas", "overseas123");
        String requestId = created.requestId();
        acceptanceService.acceptRequest(requestId);
        inventoryQueryService.dispatchInventoryQueries(requestId);
        inventoryQueryRepository.findByRequestId(requestId).forEach(q -> {
            q.setInStockQuantity(50);
            q.setRespondedAt(Instant.now());
            inventoryQueryRepository.save(q);
        });
        orderSplitService.confirmSplit(requestId, LocalDate.now());
        orderDispatchService.dispatchOrders(requestId);
        return requestId;
    }
}
