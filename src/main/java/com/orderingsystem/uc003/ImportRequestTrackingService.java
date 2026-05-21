package com.orderingsystem.uc003;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.domain.auth.UserRole;
import com.orderingsystem.domain.request.ImportRequest;
import com.orderingsystem.domain.request.ImportRequestItem;
import com.orderingsystem.domain.request.RequestStatus;
import com.orderingsystem.infrastructure.repository.ImportRequestRepository;
import com.orderingsystem.infrastructure.repository.PurchaseOrderRepository;
import com.orderingsystem.uc002.dto.ImportRequestDto;
import com.orderingsystem.uc003.dto.ImportRequestListItemDto;
import com.orderingsystem.uc003.dto.ImportRequestTrackingDetailDto;
import com.orderingsystem.uc003.dto.PurchaseOrderTrackingDto;

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
public class ImportRequestTrackingService {

    private static final String SALES_DEPARTMENT = "Sales";
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final AuthService authService;
    private final ImportRequestRepository importRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public ImportRequestTrackingService() {
        this(new AuthService(), new ImportRequestRepository(), new PurchaseOrderRepository());
    }

    public ImportRequestTrackingService(
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
        Map<String, LocalDate> deliveryByMerchandise = request.getItems().stream()
                .collect(Collectors.toMap(
                        ImportRequestItem::getMerchandiseCode,
                        ImportRequestItem::getDesiredDeliveryDate,
                        (a, b) -> a.isBefore(b) ? a : b));

        List<PurchaseOrderTrackingDto> childOrders = purchaseOrderRepository.findByRequestId(request.getRequestId())
                .stream()
                .map(order -> PurchaseOrderTrackingDto.from(
                        order,
                        deliveryByMerchandise.get(order.getMerchandiseCode())))
                .toList();

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
