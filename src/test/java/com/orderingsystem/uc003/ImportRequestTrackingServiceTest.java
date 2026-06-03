package com.orderingsystem.uc003;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.DeliveryMeans;
import com.orderingsystem.core.domain.PurchaseOrder;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import com.orderingsystem.uc002.ImportRequestService;
import com.orderingsystem.uc002.boundary.dto.CreateImportRequestLineInput;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;
import com.orderingsystem.uc003.boundary.dto.ImportRequestTrackingDetailDto;
import com.orderingsystem.uc005.ImportRequestAcceptanceService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportRequestTrackingServiceTest {

    private static final AuthService authService = new AuthService();
    private static final ImportRequestService importRequestService = new ImportRequestService();
    private static final ImportRequestAcceptanceService acceptanceService = new ImportRequestAcceptanceService();
    private static final ImportRequestTrackingService trackingService = new ImportRequestTrackingService();
    private static final PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepository();
    private static final ImportRequestRepository importRequestRepository = new ImportRequestRepository();

    @BeforeAll
    static void setUp() {
        DbManager.init("jdbc:sqlite:data/test-ordering.db", "", "");
        SchemaInitializer.apply();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterEach
    void logout() {
        authService.logout();
    }

    @AfterAll
    static void shutDown() {
        DbManager.shutdown();
    }

    @Test
    void listAndDetail_requiresSales() {
        authService.login("sales", "sales123");
        ImportRequestDto created = importRequestService.createImportRequest(List.of(
                new CreateImportRequestLineInput("P001", 30, "box", LocalDate.now().plusDays(20))));
        authService.logout();

        authService.login("overseas", "overseas123");
        acceptanceService.acceptRequest(created.requestId());
        authService.logout();

        authService.login("overseas", "overseas123");
        assertThrows(SecurityException.class, () -> trackingService.listRequests());
        authService.logout();

        authService.login("sales", "sales123");
        assertTrue(trackingService.listRequests().stream()
                .anyMatch(r -> r.requestId().equals(created.requestId())
                        && r.itemCount() == 1
                        && r.status() == RequestStatus.DANG_XU_LY));

        ImportRequestTrackingDetailDto detail =
                trackingService.getRequestDetail(created.requestId()).orElseThrow();
        assertEquals(RequestStatus.DANG_XU_LY, detail.request().status());
        assertEquals(1, detail.request().items().size());
        assertTrue(detail.childOrders().isEmpty());
    }

    @Test
    void list_filterByStatus() {
        authService.login("sales", "sales123");
        ImportRequestDto pending = importRequestService.createImportRequest(List.of(
                new CreateImportRequestLineInput("P002", 10, "pcs", LocalDate.now().plusDays(15))));

        assertTrue(trackingService.listRequests(RequestStatus.CHO_XU_LY, null, null).stream()
                .anyMatch(r -> r.requestId().equals(pending.requestId())));

        assertTrue(trackingService.listRequests(RequestStatus.DANG_XU_LY, null, null).stream()
                .noneMatch(r -> r.requestId().equals(pending.requestId())));
    }

    @Test
    void detail_childOrdersOnlyWhenDaTachDon() {
        authService.login("sales", "sales123");
        ImportRequestDto created = importRequestService.createImportRequest(List.of(
                new CreateImportRequestLineInput("P003", 50, "box", LocalDate.now().plusDays(25))));
        String requestId = created.requestId();

        purchaseOrderRepository.save(new PurchaseOrder(
                "PO-TRACK-1",
                requestId,
                DatabaseSeeder.DEMO_SITE_CODE,
                "P003",
                50,
                "box",
                DeliveryMeans.SHIP_DELIVERY));

        ImportRequestTrackingDetailDto beforeSplit =
                trackingService.getRequestDetail(requestId).orElseThrow();
        assertTrue(beforeSplit.childOrders().isEmpty());

        importRequestRepository.updateStatus(requestId, RequestStatus.DA_TACH_DON, "overseas");

        ImportRequestTrackingDetailDto detail = trackingService.getRequestDetail(requestId).orElseThrow();
        assertEquals(1, detail.childOrders().size());
        assertEquals("PO-TRACK-1", detail.childOrders().getFirst().orderId());
        assertEquals(DatabaseSeeder.DEMO_SITE_CODE, detail.childOrders().getFirst().siteCode());
        assertEquals("ship delivery", detail.childOrders().getFirst().deliveryMeansLabel());
        assertEquals(LocalDate.now().plusDays(25), detail.childOrders().getFirst().expectedDeliveryDate());
    }

    @Test
    void detail_includesActualQuantityAndDiff() {
        authService.login("sales", "sales123");
        ImportRequestDto created = importRequestService.createImportRequest(List.of(
                new CreateImportRequestLineInput("P001", 20, "pcs", LocalDate.now().plusDays(10))));
        String requestId = created.requestId();
        importRequestRepository.updateStatus(requestId, RequestStatus.DA_TACH_DON, "overseas");

        PurchaseOrder order = new PurchaseOrder(
                "PO-TRACK-2",
                requestId,
                DatabaseSeeder.DEMO_SITE_CODE,
                "P001",
                20,
                "pcs",
                DeliveryMeans.AIR_DELIVERY);
        order.setStatus(OrderStatus.SAI_LECH);
        order.setActualQuantity(18);
        order.setQuantityDiff(-2);
        purchaseOrderRepository.save(order);

        var line = trackingService.getRequestDetail(requestId).orElseThrow().childOrders().getFirst();
        assertEquals(18, line.actualQuantity());
        assertEquals(-2, line.quantityDiff());
        assertEquals(OrderStatus.SAI_LECH, line.orderStatus());
    }

    @Test
    void list_invalidDateRange_fails() {
        authService.login("sales", "sales123");
        assertThrows(IllegalArgumentException.class, () ->
                trackingService.listRequests(null, LocalDate.now().plusDays(5), LocalDate.now()));
    }
}
