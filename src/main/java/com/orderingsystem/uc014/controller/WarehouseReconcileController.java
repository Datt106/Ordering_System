package com.orderingsystem.uc014.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.core.domain.PurchaseOrder;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc014.boundary.dto.WarehouseReconcileResultDto;

public class WarehouseReconcileController {

    private final AuthService authService;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public WarehouseReconcileController() {
        this(new AuthService(), new PurchaseOrderRepository());
    }

    public WarehouseReconcileController(AuthService authService, PurchaseOrderRepository purchaseOrderRepository) {
        this.authService = authService;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    public WarehouseReconcileResultDto recordInbound(String orderId, int actualQuantity, java.time.Instant actualTime) {
        authService.requireRole(UserRole.WAREHOUSE);
        if (actualQuantity < 0) {
            throw new IllegalArgumentException("Số lượng thực nhận không được âm.");
        }

        PurchaseOrder order = purchaseOrderRepository.findById(requireOrderId(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại: " + orderId));

        if (order.getStatus() != OrderStatus.DA_XAC_NHAN && order.getStatus() != OrderStatus.DA_GUI) {
            throw new IllegalStateException("Chỉ đối chiếu đơn ở trạng thái Đã gửi/Đã xác nhận. Hiện tại: " + order.getStatus());
        }

        int diff = actualQuantity - order.getQuantityOrdered();
        order.setActualQuantity(actualQuantity);
        order.setQuantityDiff(diff);
        order.setStatus(diff == 0 ? OrderStatus.DA_NHAP_KHO : OrderStatus.SAI_LECH);
        
        order.setReconciledAt(actualTime != null ? actualTime : java.time.Instant.now());
        
        purchaseOrderRepository.save(order);

        return new WarehouseReconcileResultDto(
                order.getOrderId(),
                order.getQuantityOrdered(),
                actualQuantity,
                diff,
                order.getStatus()
        );
    }

    private static String requireOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Mã đơn hàng không được để trống.");
        }
        return orderId.trim();
    }
}
