package com.orderingsystem.uc003.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.DeliveryMeans;
import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.ImportRequestItem;
import com.orderingsystem.core.domain.PurchaseOrder;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc003.boundary.dto.ImportRequestTrackingDetailDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử hộp trắng cho UC003 – theo dõi trạng thái yêu cầu nhập hàng.
 *
 * Tập trung vào các nhánh điều kiện quan trọng của service phía sau controller:
 * - validate khoảng ngày
 * - xử lý request không thuộc Sales
 * - xử lý request đã tách đơn / chưa tách đơn
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC003 – RequestTrackController – White Box")
class RequestTrackControllerWhiteBoxTest {

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
    void listRequests_whenDateRangeValid_executesFilteredPath() {
        ImportRequest request = new ImportRequest("REQ-012", "sales", "Sales");
        request.setStatus(RequestStatus.CHO_XU_LY);
        when(importRequestRepository.findByDepartmentFiltered(eq("Sales"), eq(RequestStatus.CHO_XU_LY), any(), any()))
                .thenReturn(List.of(request));
        when(importRequestRepository.countItemsByRequestId("REQ-012")).thenReturn(3L);

        var result = controller.listRequests(RequestStatus.CHO_XU_LY,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30));

        assertEquals(1, result.size());
        assertEquals("REQ-012", result.getFirst().requestId());
        verify(importRequestRepository).findByDepartmentFiltered(eq("Sales"), eq(RequestStatus.CHO_XU_LY), any(), any());
    }

    @Test
    void listRequests_whenFromAfterTo_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.listRequests(RequestStatus.CHO_XU_LY,
                        LocalDate.of(2026, 6, 5),
                        LocalDate.of(2026, 6, 4)));
        assertTrue(ex.getMessage().contains("không được sau"));
    }

    @Test
    void getRequestDetail_whenRequestIdBlank_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.getRequestDetail("   "));
        assertTrue(ex.getMessage().contains("không được để trống"));
    }

    @Test
    void getRequestDetail_whenRequestNotInSales_returnsEmpty() {
        ImportRequest request = new ImportRequest("REQ-013", "overseas", "Overseas");
        request.setStatus(RequestStatus.DANG_XU_LY);
        when(importRequestRepository.findByIdWithItems("REQ-013")).thenReturn(Optional.of(request));

        assertTrue(controller.getRequestDetail("REQ-013").isEmpty());
        verifyNoInteractions(purchaseOrderRepository);
    }

    @Test
    void getRequestDetail_whenStatusNotSplit_skipsChildOrderBranch() {
        ImportRequest request = new ImportRequest("REQ-014", "sales", "Sales");
        request.setStatus(RequestStatus.DANG_XU_LY);
        request.getItems().add(new ImportRequestItem("M003", 5, "pcs", LocalDate.of(2026, 6, 25)));
        when(importRequestRepository.findByIdWithItems("REQ-014")).thenReturn(Optional.of(request));

        ImportRequestTrackingDetailDto detail = controller.getRequestDetail("REQ-014").orElseThrow();

        assertEquals(RequestStatus.DANG_XU_LY, detail.request().status());
        assertTrue(detail.childOrders().isEmpty());
        verifyNoInteractions(purchaseOrderRepository);
    }

    @Test
    void getRequestDetail_whenStatusSplit_executesChildOrderBranch() {
        ImportRequest request = new ImportRequest("REQ-015", "sales", "Sales");
        request.setStatus(RequestStatus.DA_TACH_DON);
        request.getItems().add(new ImportRequestItem("M001", 4, "pcs", LocalDate.of(2026, 6, 20)));
        when(importRequestRepository.findByIdWithItems("REQ-015")).thenReturn(Optional.of(request));
        when(purchaseOrderRepository.findByRequestId("REQ-015")).thenReturn(List.of(
                new PurchaseOrder("PO-015", "REQ-015", "SITE-01", "M001", 4, "pcs", DeliveryMeans.SHIP_DELIVERY)
        ));

        ImportRequestTrackingDetailDto detail = controller.getRequestDetail("REQ-015").orElseThrow();

        assertEquals(RequestStatus.DA_TACH_DON, detail.request().status());
        assertNotNull(detail.childOrders());
        assertEquals(1, detail.childOrders().size());
        verify(purchaseOrderRepository).findByRequestId("REQ-015");
    }
}
