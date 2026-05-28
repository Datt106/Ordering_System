package com.orderingsystem.uc013.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc013.boundary.dto.WarehouseOrderDto;

import java.util.List;

public class WarehouseOrderViewController {

    private final AuthService authService;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public WarehouseOrderViewController() {
        this(new AuthService(), new PurchaseOrderRepository());
    }

    public WarehouseOrderViewController(AuthService authService, PurchaseOrderRepository purchaseOrderRepository) {
        this.authService = authService;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    public List<WarehouseOrderDto> listOrders(OrderStatus status, String siteCode, String merchandiseCode) {
        authService.requireRole(UserRole.WAREHOUSE);

        return purchaseOrderRepository.findAll().stream()
                .filter(order -> status == null || order.getStatus() == status)
                .filter(order -> siteCode == null || siteCode.isBlank() || order.getSiteCode().equals(siteCode.trim()))
                .filter(order -> merchandiseCode == null || merchandiseCode.isBlank()
                        || order.getMerchandiseCode().equals(merchandiseCode.trim()))
                .map(WarehouseOrderDto::from)
                .toList();
    }
}
