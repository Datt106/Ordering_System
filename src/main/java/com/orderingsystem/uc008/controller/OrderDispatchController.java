package com.orderingsystem.uc008.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.Session;
import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.core.domain.PurchaseOrder;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc008.boundary.dto.OrderDispatchResultDto;

import java.time.Instant;
import java.util.List;

/** UC008 — Gửi đơn hàng đã tách đến các Site. */
public class OrderDispatchController {

    private final AuthService authService;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ImportRequestRepository importRequestRepository;

    public OrderDispatchController() {
        this(new AuthService(), new PurchaseOrderRepository(), new ImportRequestRepository());
    }

    public OrderDispatchController(
            AuthService authService,
            PurchaseOrderRepository purchaseOrderRepository,
            ImportRequestRepository importRequestRepository
    ) {
        this.authService = authService;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.importRequestRepository = importRequestRepository;
    }

    /** Xem trước danh sách đơn con sẵn sàng gửi (status = CHO_GUI). */
    public OrderDispatchResultDto previewToSend(String requestId) {
        authService.requireRole(UserRole.OVERSEAS);
        String id = requireRequestId(requestId);
        importRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Yêu cầu không tồn tại: " + id));

        List<PurchaseOrder> readyOrders = purchaseOrderRepository.findByRequestId(id).stream()
                .filter(order -> order.getStatus() == OrderStatus.CHO_GUI)
                .toList();
        if (readyOrders.isEmpty()) {
            throw new IllegalStateException("Không có đơn con ở trạng thái Chờ gửi cho yêu cầu: " + id);
        }
        return toResult(id, readyOrders, 0, null);
    }

    /** Xác nhận gửi: CHO_GUI -> DA_GUI; yêu cầu gốc -> DA_TACH_DON. */
    public OrderDispatchResultDto dispatchOrders(String requestId) {
        authService.requireRole(UserRole.OVERSEAS);
        String id = requireRequestId(requestId);

        importRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Yêu cầu không tồn tại: " + id));

        List<PurchaseOrder> all = purchaseOrderRepository.findByRequestId(id);
        if (all.isEmpty()) {
            throw new IllegalStateException("Yêu cầu chưa có đơn con để gửi: " + id);
        }

        List<PurchaseOrder> ready = all.stream()
                .filter(order -> order.getStatus() == OrderStatus.CHO_GUI)
                .toList();
        if (ready.isEmpty()) {
            throw new IllegalStateException("Tất cả đơn con đã được gửi hoặc không hợp lệ: " + id);
        }

        Instant sentAt = Instant.now();
        ready.forEach(order -> {
            order.setStatus(OrderStatus.DA_GUI);
            order.setSentAt(sentAt);
        });
        purchaseOrderRepository.saveAll(ready);

        importRequestRepository.updateStatus(id, RequestStatus.DA_TACH_DON, Session.getUsername());

        return toResult(id, purchaseOrderRepository.findByRequestId(id), ready.size(), sentAt);
    }

    private static OrderDispatchResultDto toResult(
            String requestId,
            List<PurchaseOrder> orders,
            int sentOrders,
            Instant sentAt
    ) {
        List<OrderDispatchResultDto.OrderDispatchLineDto> lines = orders.stream()
                .map(order -> new OrderDispatchResultDto.OrderDispatchLineDto(
                        order.getOrderId(),
                        order.getSiteCode(),
                        order.getMerchandiseCode(),
                        order.getQuantityOrdered(),
                        order.getUnit(),
                        order.getDeliveryMeans().toExternalValue(),
                        order.getStatus()
                ))
                .toList();
        return new OrderDispatchResultDto(requestId, orders.size(), sentOrders, sentAt, lines);
    }

    private static String requireRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Mã yêu cầu không được để trống.");
        }
        return requestId.trim();
    }
}
