package com.orderingsystem.uc003.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.ImportRequestItem;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử hộp trắng cho UC003 – RequestTrackController.
 *
 * Kỹ thuật áp dụng:
 * - Phân tích đường đi điều khiển
 * - Độ đo C1 cho nhánh if/else trong toDetail()
 *
 * Full class name: com.orderingsystem.uc003.controller.RequestTrackControllerWhiteBoxTest
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
    void getRequestDetail_whenDaTachDon_executesChildOrderBranch() {
        ImportRequest request = new ImportRequest("REQ-010", "sales", "Sales");
        request.setStatus(RequestStatus.DA_TACH_DON);
        request.getItems().add(new ImportRequestItem("M001", 4, "pcs", LocalDate.of(2026, 6, 20)));
        request.getItems().add(new ImportRequestItem("M001", 8, "pcs", LocalDate.of(2026, 6, 18)));
        request.getItems().add(new ImportRequestItem("M002", 2, "box", LocalDate.of(2026, 6, 22)));

        when(importRequestRepository.findByIdWithItems("REQ-010")).thenReturn(Optional.of(request));
        when(purchaseOrderRepository.findByRequestId("REQ-010")).thenReturn(List.of());

        ImportRequestTrackingDetailDto detail = controller.getRequestDetail("REQ-010").orElseThrow();

        assertEquals(RequestStatus.DA_TACH_DON, detail.request().status());
        verify(purchaseOrderRepository).findByRequestId("REQ-010");
        assertNotNull(detail.childOrders());
    }

    @Test
    void getRequestDetail_whenStatusNotSplit_skipsChildOrderBranch() {
        ImportRequest request = new ImportRequest("REQ-011", "sales", "Sales");
        request.setStatus(RequestStatus.DANG_XU_LY);
        request.getItems().add(new ImportRequestItem("M003", 5, "pcs", LocalDate.of(2026, 6, 25)));
        when(importRequestRepository.findByIdWithItems("REQ-011")).thenReturn(Optional.of(request));

        ImportRequestTrackingDetailDto detail = controller.getRequestDetail("REQ-011").orElseThrow();

        assertEquals(RequestStatus.DANG_XU_LY, detail.request().status());
        assertTrue(detail.childOrders().isEmpty());
        verifyNoInteractions(purchaseOrderRepository);
    }

    @Test
    void validateDateRange_whenFromAfterTo_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.listRequests(RequestStatus.CHO_XU_LY,
                        LocalDate.of(2026, 6, 5),
                        LocalDate.of(2026, 6, 4)));
        assertTrue(ex.getMessage().contains("không được sau"));
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
        verify(importRequestRepository).findByDepartmentFiltered(eq("Sales"), eq(RequestStatus.CHO_XU_LY), any(), any());
    }
}
