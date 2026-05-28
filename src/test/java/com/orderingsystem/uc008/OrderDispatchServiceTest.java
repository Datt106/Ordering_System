package com.orderingsystem.uc008;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
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
import com.orderingsystem.uc008.boundary.dto.OrderDispatchResultDto;
import com.orderingsystem.uc009.SiteMerchandiseService;
import com.orderingsystem.uc010.SiteShippingService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderDispatchServiceTest {

    private static final AuthService authService = new AuthService();
    private static final ImportRequestService importRequestService = new ImportRequestService();
    private static final ImportRequestAcceptanceService acceptanceService = new ImportRequestAcceptanceService();
    private static final InventoryQueryService inventoryQueryService = new InventoryQueryService();
    private static final SiteShippingService siteShippingService = new SiteShippingService();
    private static final SiteMerchandiseService siteMerchandiseService = new SiteMerchandiseService();
    private static final OrderSplitService orderSplitService = new OrderSplitService();
    private static final OrderDispatchService orderDispatchService = new OrderDispatchService();
    private static final PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepository();
    private static final ImportRequestRepository importRequestRepository = new ImportRequestRepository();
    private static final InventoryQueryRepository inventoryQueryRepository = new InventoryQueryRepository();

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
        authService.login("overseas", "overseas123");
    }

    @Test
    void dispatchOrders_updatesOrderAndRequestStatus() {
        String requestId = prepareSplitOrders();

        OrderDispatchResultDto preview = orderDispatchService.previewToSend(requestId);
        assertTrue(preview.totalOrders() > 0);
        assertTrue(preview.lines().stream().allMatch(line -> line.status() == OrderStatus.CHO_GUI));

        OrderDispatchResultDto result = orderDispatchService.dispatchOrders(requestId);
        assertEquals(preview.totalOrders(), result.sentOrders());
        assertTrue(result.lines().stream().allMatch(line -> line.status() == OrderStatus.DA_GUI));

        assertEquals(RequestStatus.DA_TACH_DON,
                importRequestRepository.findById(requestId).orElseThrow().getStatus());
    }

    @Test
    void dispatchOrders_requiresOverseasRole() {
        authService.logout();
        authService.login("sales", "sales123");
        assertThrows(SecurityException.class, () -> orderDispatchService.previewToSend("REQ-ANY"));
    }

    private String prepareSplitOrders() {
        authService.logout();
        authService.login("site01", "site123");
        siteShippingService.updateMyShipping(10, 3);
        try {
            siteMerchandiseService.addMerchandise("P001");
        } catch (IllegalArgumentException ignored) {
            // already exists in repeated test runs
        }
        authService.logout();

        authService.login("sales", "sales123");
        ImportRequestDto created = importRequestService.createImportRequest(List.of(
                new CreateImportRequestLineInput("P001", 40, "unit", LocalDate.now().plusDays(20))));
        authService.logout();

        authService.login("overseas", "overseas123");
        String requestId = created.requestId();
        acceptanceService.acceptRequest(requestId);
        inventoryQueryService.dispatchInventoryQueries(requestId);

        // deterministic response: all queries have enough stock
        inventoryQueryRepository.findByRequestId(requestId).forEach(query -> {
            query.setInStockQuantity(100);
            query.setRespondedAt(Instant.now());
            inventoryQueryRepository.save(query);
        });

        orderSplitService.confirmSplit(requestId, LocalDate.now());
        return requestId;
    }
}
