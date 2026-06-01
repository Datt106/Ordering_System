package com.orderingsystem.uc005.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.Session;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * UC005 — Bộ phận Đặt hàng quốc tế xem và tiếp nhận yêu cầu nhập hàng (Chờ xử lý → Đang xử lý).
 */
public class RequestAcceptController {

    private final AuthService authService;
    private final ImportRequestRepository importRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public RequestAcceptController() {
        this(new AuthService(), new ImportRequestRepository(), new PurchaseOrderRepository());
    }

    public RequestAcceptController(
            AuthService authService,
            ImportRequestRepository importRequestRepository,
            PurchaseOrderRepository purchaseOrderRepository
    ) {
        this.authService = authService;
        this.importRequestRepository = importRequestRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    /** Danh sách yêu cầu Chờ xử lý, mới nhất trước (không kèm dòng mặt hàng). */
    public List<ImportRequestDto> listPendingRequests() {
        authService.requireRole(UserRole.OVERSEAS);
        return importRequestRepository.findByStatus(RequestStatus.CHO_XU_LY).stream()
                .map(ImportRequestDto::fromSummary)
                .toList();
    }

    /** Danh sách yêu cầu Đang xử lý — dùng cho UC006 truy vấn tồn kho. */
    public List<ImportRequestDto> listProcessingRequests() {
        authService.requireRole(UserRole.OVERSEAS);
        return importRequestRepository.findByStatus(RequestStatus.DANG_XU_LY).stream()
                .map(ImportRequestDto::fromSummary)
                .toList();
    }

    /** Yêu cầu có ít nhất một đơn con Chờ gửi — dùng cho UC008 (tránh chọn REQ chưa tách đơn). */
    public List<ImportRequestDto> listRequestsReadyForDispatch() {
        authService.requireRole(UserRole.OVERSEAS);
        Set<String> requestIds = new LinkedHashSet<>(
                purchaseOrderRepository.findDistinctRequestIdsByStatus(OrderStatus.CHO_GUI));
        List<ImportRequestDto> result = new ArrayList<>();
        for (String requestId : requestIds) {
            importRequestRepository.findById(requestId)
                    .map(ImportRequestDto::fromSummary)
                    .ifPresent(result::add);
        }
        return result;
    }

    /** Chi tiết yêu cầu (kèm danh sách mặt hàng) — xem trước khi tiếp nhận. */
    public Optional<ImportRequestDto> getRequest(String requestId) {
        authService.requireRole(UserRole.OVERSEAS);
        return importRequestRepository.findByIdWithItems(requireRequestId(requestId))
                .map(ImportRequestDto::from);
    }

    /** Tiếp nhận xử lý: ghi nhận người tiếp nhận và thời điểm. */
    public ImportRequestDto acceptRequest(String requestId) {
        authService.requireRole(UserRole.OVERSEAS);
        AuthenticatedUser user = Session.requireCurrentUser();
        String id = requireRequestId(requestId);
        importRequestRepository.acceptForProcessing(id, user.username());
        return ImportRequestDto.from(importRequestRepository.findByIdWithItems(id).orElseThrow());
    }

    private static String requireRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Mã yêu cầu không được để trống.");
        }
        return requestId.trim();
    }
}
