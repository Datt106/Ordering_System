package com.orderingsystem.uc012.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.Session;
import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.core.domain.PurchaseOrder;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc012.boundary.dto.SiteOrderDto;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

public class SiteOrderConfirmController {

    private final AuthService authService;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public SiteOrderConfirmController() {
        this(new AuthService(), new PurchaseOrderRepository());
    }

    public SiteOrderConfirmController(AuthService authService, PurchaseOrderRepository purchaseOrderRepository) {
        this.authService = authService;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    public List<SiteOrderDto> listMyIncomingOrders() {
        authService.requireRole(UserRole.SITE);
        String siteCode = requireSiteCode();
        return purchaseOrderRepository
                .findBySiteCodeAndStatuses(siteCode, EnumSet.of(OrderStatus.DA_GUI))
                .stream()
                .map(SiteOrderDto::from)
                .toList();
    }

    public SiteOrderDto confirmOrder(String orderId) {
        authService.requireRole(UserRole.SITE);
        PurchaseOrder order = requireOwnOrder(orderId);
        if (order.getStatus() != OrderStatus.DA_GUI) {
            throw new IllegalStateException("Chỉ xác nhận đơn ở trạng thái Đã gửi. Hiện tại: " + order.getStatus());
        }
        order.setStatus(OrderStatus.DA_XAC_NHAN);
        order.setConfirmedAt(Instant.now());
        purchaseOrderRepository.save(order);
        return SiteOrderDto.from(order);
    }

    public SiteOrderDto rejectOrder(String orderId) {
        authService.requireRole(UserRole.SITE);
        PurchaseOrder order = requireOwnOrder(orderId);
        if (order.getStatus() != OrderStatus.DA_GUI) {
            throw new IllegalStateException("Chỉ từ chối đơn ở trạng thái Đã gửi. Hiện tại: " + order.getStatus());
        }
        order.setStatus(OrderStatus.TU_CHOI);
        order.setConfirmedAt(Instant.now());
        purchaseOrderRepository.save(order);
        return SiteOrderDto.from(order);
    }

    private PurchaseOrder requireOwnOrder(String orderId) {
        String siteCode = requireSiteCode();
        PurchaseOrder order = purchaseOrderRepository.findById(requireOrderId(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại: " + orderId));
        if (!siteCode.equals(order.getSiteCode())) {
            throw new SecurityException("Đơn hàng không thuộc Site của bạn.");
        }
        return order;
    }

    private static String requireSiteCode() {
        String siteCode = Session.getSiteCode().orElse(null);
        if (siteCode == null || siteCode.isBlank()) {
            throw new IllegalStateException("Tài khoản Site chưa gắn mã Site.");
        }
        return siteCode.trim();
    }

    private static String requireOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Mã đơn hàng không được để trống.");
        }
        return orderId.trim();
    }
}
