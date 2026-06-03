package com.orderingsystem.uc003.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.ImportRequestItem;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;
import com.orderingsystem.uc003.boundary.dto.ImportRequestListItemDto;
import com.orderingsystem.uc003.boundary.dto.ImportRequestTrackingDetailDto;
import com.orderingsystem.uc003.boundary.dto.PurchaseOrderTrackingDto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UC003 — Bộ phận Bán hàng theo dõi trạng thái yêu cầu nhập hàng (read-only).
 */
public class RequestTrackController {

    private static final String SALES_DEPARTMENT = "Sales";
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final AuthService authService;
    private final ImportRequestRepository importRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public RequestTrackController() {
        this(new AuthService(), new ImportRequestRepository(), new PurchaseOrderRepository());
    }

    public RequestTrackController(
            AuthService authService,
            ImportRequestRepository importRequestRepository,
            PurchaseOrderRepository purchaseOrderRepository
    ) {
        this.authService = authService;
        this.importRequestRepository = importRequestRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    /** Tất cả yêu cầu của bộ phận Bán hàng, mới nhất trước. */
    public List<ImportRequestListItemDto> listRequests() {
        return listRequests(null, null, null);
    }

    /**
     * Danh sách có lọc (FR-02.4): trạng thái và/hoặc ngày tạo (inclusive).
     */
    public List<ImportRequestListItemDto> listRequests(
            RequestStatus status,
            LocalDate createdFrom,
            LocalDate createdTo
    ) {
        authService.requireRole(UserRole.SALES);
        validateDateRange(createdFrom, createdTo);

        Instant fromInstant = createdFrom == null ? null : createdFrom.atStartOfDay(ZONE).toInstant();
        Instant toInstant = createdTo == null ? null : createdTo.plusDays(1).atStartOfDay(ZONE).toInstant();

        List<ImportRequest> requests = importRequestRepository.findByDepartmentFiltered(
                SALES_DEPARTMENT, status, fromInstant, toInstant);

        return requests.stream()
                .map(this::toListItem)
                .toList();
    }

    /** Chi tiết yêu cầu: mặt hàng, trạng thái, đơn con (nếu đã tách đơn). */
    public Optional<ImportRequestTrackingDetailDto> getRequestDetail(String requestId) {
        authService.requireRole(UserRole.SALES);
        String id = requireRequestId(requestId);

        return importRequestRepository.findByIdWithItems(id)
                .filter(r -> SALES_DEPARTMENT.equals(r.getDepartment()))
                .map(this::toDetail);
    }

    private ImportRequestListItemDto toListItem(ImportRequest request) {
        int itemCount = (int) importRequestRepository.countItemsByRequestId(request.getRequestId());
        return new ImportRequestListItemDto(
                request.getRequestId(),
                request.getCreatedAt(),
                itemCount,
                request.getStatus()
        );
    }

    private ImportRequestTrackingDetailDto toDetail(ImportRequest request) {
        ImportRequestDto requestDto = ImportRequestDto.from(request);
        List<PurchaseOrderTrackingDto> childOrders = List.of();
        if (request.getStatus() == RequestStatus.DA_TACH_DON) {
            Map<String, LocalDate> deliveryByMerchandise = request.getItems().stream()
                    .collect(Collectors.toMap(
                            ImportRequestItem::getMerchandiseCode,
                            ImportRequestItem::getDesiredDeliveryDate,
                            (a, b) -> a.isBefore(b) ? a : b));
            childOrders = purchaseOrderRepository.findByRequestId(request.getRequestId())
                    .stream()
                    .map(order -> PurchaseOrderTrackingDto.from(
                            order,
                            deliveryByMerchandise.get(order.getMerchandiseCode())))
                    .toList();
        }
        return new ImportRequestTrackingDetailDto(requestDto, childOrders);
    }

    private static void validateDateRange(LocalDate createdFrom, LocalDate createdTo) {
        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            throw new IllegalArgumentException("Ngày bắt đầu lọc không được sau ngày kết thúc.");
        }
    }

    private static String requireRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Mã yêu cầu không được để trống.");
        }
        return requestId.trim();
    }
}
