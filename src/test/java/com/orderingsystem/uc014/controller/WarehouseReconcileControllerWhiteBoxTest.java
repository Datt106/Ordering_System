package com.orderingsystem.uc014.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.core.domain.PurchaseOrder;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseReconcileControllerWhiteBoxTest {

    @Mock
    private AuthService authService;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @InjectMocks
    private WarehouseReconcileController controller;

    private PurchaseOrder order;

    @BeforeEach
    void setUp() {
        doNothing().when(authService).requireRole(UserRole.WAREHOUSE);
        order = new PurchaseOrder(
                "PO-20260604-010",
                "REQ-20260604-010",
                "WH-01",
                "MCH-010",
                10,
                "BOX",
                com.orderingsystem.core.domain.DeliveryMeans.SHIP_DELIVERY
        );
        order.setStatus(OrderStatus.DA_XAC_NHAN);
    }

    @Test
    void branchNegativeQuantityTrue() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.recordInbound("PO-20260604-010", -1));
    }

    @Test
    void branchBlankOrderIdTrue() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.recordInbound("", 1));
    }

    @Test
    void branchOrderNotFoundTrue() {
        when(purchaseOrderRepository.findById("PO-404")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> controller.recordInbound("PO-404", 1));
    }

    @Test
    void branchWrongStatusTrue() {
        order.setStatus(OrderStatus.CHO_GUI);
        when(purchaseOrderRepository.findById("PO-20260604-010")).thenReturn(Optional.of(order));
        assertThrows(IllegalStateException.class,
                () -> controller.recordInbound("PO-20260604-010", 1));
    }

    @Test
    void branchDiffEqualsZeroFalsePath() {
        when(purchaseOrderRepository.findById("PO-20260604-010")).thenReturn(Optional.of(order));
        var result = controller.recordInbound("PO-20260604-010", 10);

        assertEquals(OrderStatus.DA_NHAP_KHO, result.status());
        assertEquals(0, result.quantityDiff());
        verify(purchaseOrderRepository).save(order);
    }

    @Test
    void branchDiffNotEqualsZeroFalsePath() {
        when(purchaseOrderRepository.findById("PO-20260604-010")).thenReturn(Optional.of(order));
        var result = controller.recordInbound("PO-20260604-010", 13);

        assertEquals(OrderStatus.SAI_LECH, result.status());
        assertEquals(3, result.quantityDiff());
    }
}
