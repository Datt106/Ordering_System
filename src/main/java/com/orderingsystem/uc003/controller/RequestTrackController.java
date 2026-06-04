package com.orderingsystem.uc003.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc003.ImportRequestTrackingService;
import com.orderingsystem.uc003.boundary.RequestTrackingUseCase;
import com.orderingsystem.uc003.boundary.dto.ImportRequestListItemDto;
import com.orderingsystem.uc003.boundary.dto.ImportRequestTrackingDetailDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * UC003 — Bộ phận Bán hàng theo dõi trạng thái yêu cầu nhập hàng (read-only).
 */
public class RequestTrackController {

    private final RequestTrackingUseCase requestTrackingUseCase;

    public RequestTrackController() {
        this(new ImportRequestTrackingService());
    }

    public RequestTrackController(
            AuthService authService,
            ImportRequestRepository importRequestRepository,
            PurchaseOrderRepository purchaseOrderRepository
    ) {
        this(new ImportRequestTrackingService(
                authService,
                importRequestRepository,
                purchaseOrderRepository
        ));
    }

    public RequestTrackController(RequestTrackingUseCase requestTrackingUseCase) {
        this.requestTrackingUseCase = Objects.requireNonNull(
                requestTrackingUseCase, "requestTrackingUseCase");
    }

    /** Tất cả yêu cầu của bộ phận Bán hàng, mới nhất trước. */
    public List<ImportRequestListItemDto> listRequests() {
        return requestTrackingUseCase.listRequests();
    }

    /**
     * Danh sách có lọc (FR-02.4): trạng thái và/hoặc ngày tạo (inclusive).
     */
    public List<ImportRequestListItemDto> listRequests(
            RequestStatus status,
            LocalDate createdFrom,
            LocalDate createdTo
    ) {
        return requestTrackingUseCase.listRequests(status, createdFrom, createdTo);
    }

    /** Chi tiết yêu cầu: mặt hàng, trạng thái, đơn con (nếu đã tách đơn). */
    public Optional<ImportRequestTrackingDetailDto> getRequestDetail(String requestId) {
        return requestTrackingUseCase.getRequestDetail(requestId);
    }
}
