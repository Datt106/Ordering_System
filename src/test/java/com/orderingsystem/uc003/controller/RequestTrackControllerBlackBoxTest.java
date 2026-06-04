package com.orderingsystem.uc003.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.ImportRequestItem;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử hộp đen cho UC003 – RequestTrackController.
 *
 * Kỹ thuật áp dụng:
 * - Phân lớp tương đương
 * - Giá trị biên
 *
 * Full class name: com.orderingsystem.uc003.controller.RequestTrackControllerBlackBoxTest
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
    void listRequests_whenNoFilter_returnsMappedItems() {
        ImportRequest request = new ImportRequest("REQ-001", "sales", "Sales");
        request.setCreatedAt(Instant.parse("2026-06-01T10:00:00Z"));
        request.setStatus(RequestStatus.CHO_XU_LY);
        when(importRequestRepository.findByDepartmentFiltered(eq("Sales"), isNull(), isNull(), isNull()))
                .thenReturn(List.of(request));
        when(importRequestRepository.countItemsByRequestId("REQ-001")).thenReturn(2L);

        List<ImportRequestListItemDto> result = controller.listRequests();

        assertEquals(1, result.size());
        assertEquals("REQ-001", result.get(0).requestId());
        assertEquals(2, result.get(0).itemCount());
        assertEquals(RequestStatus.CHO_XU_LY, result.get(0).status());
    }

    @Test
    void listRequests_whenCreatedDateIncluded_usesInclusiveRange() {
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
        verify(importRequestRepository).findByDepartmentFiltered(eq("Sales"), eq(RequestStatus.CHO_XU_LY), any(), any());
    }

    @Test
    void getRequestDetail_whenBlankId_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.getRequestDetail("   "));
        assertTrue(ex.getMessage().contains("không được để trống"));
    }

    @Test
    void getRequestDetail_whenRequestBelongsToOtherDepartment_returnsEmpty() {
        ImportRequest request = new ImportRequest("REQ-003", "overseas", "Overseas");
        request.setStatus(RequestStatus.CHO_XU_LY);
        when(importRequestRepository.findByIdWithItems("REQ-003")).thenReturn(Optional.of(request));

        assertTrue(controller.getRequestDetail("REQ-003").isEmpty());
    }

    @Test
    void getRequestDetail_whenSalesRequest_returnsDetailWithoutChildOrdersForNonSplitStatus() {
        ImportRequest request = new ImportRequest("REQ-004", "sales", "Sales");
        request.setStatus(RequestStatus.DANG_XU_LY);
        request.getItems().add(new ImportRequestItem("P001", 3, "pcs", LocalDate.of(2026, 6, 10)));
        when(importRequestRepository.findByIdWithItems("REQ-004")).thenReturn(Optional.of(request));

        ImportRequestTrackingDetailDto detail = controller.getRequestDetail("REQ-004").orElseThrow();

        assertEquals("REQ-004", detail.request().requestId());
        assertTrue(detail.childOrders().isEmpty());
        verifyNoInteractions(purchaseOrderRepository);
    }
}
