package com.orderingsystem.uc014.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.core.domain.PurchaseOrder;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc014.boundary.dto.WarehouseReconcileResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseReconcileControllerBlackBoxTest {

    @Mock
    private AuthService authService;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @InjectMocks
    private WarehouseReconcileController controller;

    private PurchaseOrder confirmedOrder;

    @BeforeEach
    void setUp() {
        confirmedOrder = new PurchaseOrder(
                "PO-20260604-001",
                "REQ-20260604-001",
                "WH-01",
                "MCH-001",
                10,
                "BOX",
                com.orderingsystem.core.domain.DeliveryMeans.SHIP_DELIVERY
        );
        confirmedOrder.setStatus(OrderStatus.DA_XAC_NHAN);
        doNothing().when(authService).requireRole(UserRole.WAREHOUSE);
        lenient().doNothing().when(purchaseOrderRepository).save(any(PurchaseOrder.class));
    }

    @Test
    void recordInbound_rejectsNegativeActualQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.recordInbound("PO-20260604-001", -1));
    }

    @Test
    void recordInbound_rejectsBlankOrderId() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.recordInbound("   ", 1));
    }

    @Test
    void recordInbound_rejectsNonExistingOrder() {
        when(purchaseOrderRepository.findById("PO-404")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> controller.recordInbound("PO-404", 1));
    }

    @Test
    void recordInbound_rejectsWrongOrderStatus() {
        confirmedOrder.setStatus(OrderStatus.CHO_GUI);
        when(purchaseOrderRepository.findById("PO-20260604-001")).thenReturn(Optional.of(confirmedOrder));

        assertThrows(IllegalStateException.class,
                () -> controller.recordInbound("PO-20260604-001", 10));
    }

    @Test
    void recordInbound_acceptsMatchingQuantityAndReturnsSuccessDto() {
        when(purchaseOrderRepository.findById("PO-20260604-001")).thenReturn(Optional.of(confirmedOrder));
        WarehouseReconcileResultDto result = controller.recordInbound("PO-20260604-001", 10);

        assertEquals("PO-20260604-001", result.orderId());
        assertEquals(10, result.orderedQuantity());
        assertEquals(10, result.actualQuantity());
        assertEquals(0, result.quantityDiff());
        assertEquals(OrderStatus.DA_NHAP_KHO, result.status());
        verify(purchaseOrderRepository).save(confirmedOrder);
    }

    @Test
    void recordInbound_acceptsMismatchedQuantityAndMarksDiscrepancy() {
        when(purchaseOrderRepository.findById("PO-20260604-001")).thenReturn(Optional.of(confirmedOrder));
        WarehouseReconcileResultDto result = controller.recordInbound("PO-20260604-001", 12);

        assertEquals(2, result.quantityDiff());
        assertEquals(OrderStatus.SAI_LECH, result.status());
    }
}
