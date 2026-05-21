package com.orderingsystem.uc003;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.domain.order.DeliveryMeans;
import com.orderingsystem.domain.order.PurchaseOrder;
import com.orderingsystem.domain.request.RequestStatus;
import com.orderingsystem.infrastructure.jpa.JpaBootstrap;
import com.orderingsystem.infrastructure.repository.PurchaseOrderRepository;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import com.orderingsystem.uc002.ImportRequestService;
import com.orderingsystem.uc002.dto.CreateImportRequestLineInput;
import com.orderingsystem.uc002.dto.ImportRequestDto;
import com.orderingsystem.uc003.dto.ImportRequestTrackingDetailDto;
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

    @BeforeAll
    static void setUp() {
        JpaBootstrap.init();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterEach
    void logout() {
        authService.logout();
    }

    @AfterAll
    static void shutDown() {
        JpaBootstrap.shutdown();
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
    void detail_includesChildOrders() {
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

        ImportRequestTrackingDetailDto detail = trackingService.getRequestDetail(requestId).orElseThrow();
        assertEquals(1, detail.childOrders().size());
        assertEquals("PO-TRACK-1", detail.childOrders().getFirst().orderId());
        assertEquals(DatabaseSeeder.DEMO_SITE_CODE, detail.childOrders().getFirst().siteCode());
        assertEquals("ship delivery", detail.childOrders().getFirst().deliveryMeansLabel());
        assertEquals(LocalDate.now().plusDays(25), detail.childOrders().getFirst().expectedDeliveryDate());
    }

    @Test
    void list_invalidDateRange_fails() {
        authService.login("sales", "sales123");
        assertThrows(IllegalArgumentException.class, () ->
                trackingService.listRequests(null, LocalDate.now().plusDays(5), LocalDate.now()));
    }
}
