package com.orderingsystem.uc003.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.DeliveryMeans;
import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.ImportRequestItem;
import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.core.domain.PurchaseOrder;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc003.boundary.dto.ImportRequestListItemDto;
import com.orderingsystem.uc003.boundary.dto.ImportRequestTrackingDetailDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử hộp đen cho UC003 – theo dõi trạng thái yêu cầu nhập hàng.
 *
 * Mục tiêu:
 * - kiểm tra đầu vào/đầu ra quan sát được của use case
 * - không phụ thuộc vào chi tiết triển khai bên trong
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC003 – RequestTrackController – Black Box")
class RequestTrackControllerBlackBoxTest {

    @Mock
    private AuthService authService;

    @Mock
    private ImportRequestRepository importRequestRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    private RequestTrackController controller;

    @BeforeEach
    void setUp() {
        controller = new RequestTrackController(authService, importRequestRepository, purchaseOrderRepository);
        doNothing().when(authService).requireRole(UserRole.SALES);
    }

    @Test
    void listRequests_withoutFilter_returnsSalesRequestsAsDtos() {
        ImportRequest request = new ImportRequest("REQ-001", "sales", "Sales");
        request.setCreatedAt(Instant.parse("2026-06-01T10:00:00Z"));
        request.setStatus(RequestStatus.DA_TACH_DON);

        when(importRequestRepository.findByDepartmentFiltered(eq("Sales"), isNull(), isNull(), isNull()))
                .thenReturn(List.of(request));
        when(importRequestRepository.countItemsByRequestId("REQ-001")).thenReturn(2L);

        List<ImportRequestListItemDto> result = controller.listRequests();

        assertEquals(1, result.size());
        assertEquals("REQ-001", result.getFirst().requestId());
        assertEquals(2, result.getFirst().itemCount());
        assertEquals(RequestStatus.DA_TACH_DON, result.getFirst().status());
    }

    @Test
    void listRequests_withStatusAndBoundaryDate_returnsMatchingItems() {
        ImportRequest request = new ImportRequest("REQ-002", "sales", "Sales");
        request.setCreatedAt(Instant.parse("2026-06-04T01:00:00Z"));
        request.setStatus(RequestStatus.CHO_XU_LY);

        when(importRequestRepository.findByDepartmentFiltered(eq("Sales"), eq(RequestStatus.CHO_XU_LY), any(), any()))
                .thenReturn(List.of(request));
        when(importRequestRepository.countItemsByRequestId("REQ-002")).thenReturn(1L);

        List<ImportRequestListItemDto> result = controller.listRequests(
                RequestStatus.CHO_XU_LY,
                LocalDate.of(2026, 6, 4),
                LocalDate.of(2026, 6, 4)
        );

        assertEquals(1, result.size());
        assertEquals("REQ-002", result.getFirst().requestId());
        verify(importRequestRepository).findByDepartmentFiltered(eq("Sales"), eq(RequestStatus.CHO_XU_LY), any(), any());
    }

    @Test
    void getRequestDetail_blankRequestId_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.getRequestDetail("   "));
        assertTrue(ex.getMessage().contains("không được để trống"));
    }

    @Test
    void getRequestDetail_requestFromOtherDepartment_returnsEmptyOptional() {
        ImportRequest request = new ImportRequest("REQ-003", "overseas", "Overseas");
        request.setStatus(RequestStatus.CHO_XU_LY);
        when(importRequestRepository.findByIdWithItems("REQ-003")).thenReturn(Optional.of(request));

        assertTrue(controller.getRequestDetail("REQ-003").isEmpty());
        verifyNoInteractions(purchaseOrderRepository);
    }

    @Test
    void getRequestDetail_salesRequestWithoutSplitOrders_returnsEmptyChildOrders() {
        ImportRequest request = new ImportRequest("REQ-004", "sales", "Sales");
        request.setStatus(RequestStatus.DANG_XU_LY);
        request.getItems().add(new ImportRequestItem("P001", 3, "pcs", LocalDate.of(2026, 6, 10)));
        when(importRequestRepository.findByIdWithItems("REQ-004")).thenReturn(Optional.of(request));

        ImportRequestTrackingDetailDto detail = controller.getRequestDetail("REQ-004").orElseThrow();

        assertEquals("REQ-004", detail.request().requestId());
        assertEquals(RequestStatus.DANG_XU_LY, detail.request().status());
        assertTrue(detail.childOrders().isEmpty());
        verifyNoInteractions(purchaseOrderRepository);
    }

    @Test
    void getRequestDetail_splitRequest_returnsChildOrders() {
        ImportRequest request = new ImportRequest("REQ-005", "sales", "Sales");
        request.setStatus(RequestStatus.DA_TACH_DON);
        request.getItems().add(new ImportRequestItem("P002", 5, "box", LocalDate.of(2026, 6, 20)));
        when(importRequestRepository.findByIdWithItems("REQ-005")).thenReturn(Optional.of(request));
        when(purchaseOrderRepository.findByRequestId("REQ-005")).thenReturn(List.of(
                new PurchaseOrder("PO-001", "REQ-005", "SITE-01", "P002", 5, "box", DeliveryMeans.SHIP_DELIVERY)
        ));

        ImportRequestTrackingDetailDto detail = controller.getRequestDetail("REQ-005").orElseThrow();

        assertEquals(RequestStatus.DA_TACH_DON, detail.request().status());
        assertFalse(detail.childOrders().isEmpty());
        assertEquals("PO-001", detail.childOrders().getFirst().orderId());
        assertEquals(OrderStatus.CHO_GUI, detail.childOrders().getFirst().orderStatus());
        verify(purchaseOrderRepository).findByRequestId("REQ-005");
    }
}
