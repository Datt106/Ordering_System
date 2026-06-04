package com.orderingsystem.uc003;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc003.boundary.RequestTrackingUseCase;
import com.orderingsystem.uc003.boundary.dto.ImportRequestListItemDto;
import com.orderingsystem.uc003.boundary.dto.ImportRequestTrackingDetailDto;
import com.orderingsystem.uc003.mapper.RequestTrackingDtoMapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Application service for UC003 request tracking.
 */
public class ImportRequestTrackingService implements RequestTrackingUseCase {

    private static final String SALES_DEPARTMENT = "Sales";
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final AuthService authService;
    private final ImportRequestRepository importRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final RequestTrackingDtoMapper dtoMapper;

    public ImportRequestTrackingService() {
        this(
                new AuthService(),
                new ImportRequestRepository(),
                new PurchaseOrderRepository(),
                new RequestTrackingDtoMapper()
        );
    }

    public ImportRequestTrackingService(
            AuthService authService,
            ImportRequestRepository importRequestRepository,
            PurchaseOrderRepository purchaseOrderRepository
    ) {
        this(
                authService,
                importRequestRepository,
                purchaseOrderRepository,
                new RequestTrackingDtoMapper()
        );
    }

    public ImportRequestTrackingService(
            AuthService authService,
            ImportRequestRepository importRequestRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            RequestTrackingDtoMapper dtoMapper
    ) {
        this.authService = Objects.requireNonNull(authService, "authService");
        this.importRequestRepository = Objects.requireNonNull(
                importRequestRepository, "importRequestRepository");
        this.purchaseOrderRepository = Objects.requireNonNull(
                purchaseOrderRepository, "purchaseOrderRepository");
        this.dtoMapper = Objects.requireNonNull(dtoMapper, "dtoMapper");
    }

    @Override
    public List<ImportRequestListItemDto> listRequests() {
        return listRequests(null, null, null);
    }

    @Override
    public List<ImportRequestListItemDto> listRequests(
            RequestStatus status,
            LocalDate createdFrom,
            LocalDate createdTo
    ) {
        authService.requireRole(UserRole.SALES);
        RequestTrackingFilter filter = new RequestTrackingFilter(status, createdFrom, createdTo);

        List<ImportRequest> requests = importRequestRepository.findByDepartmentFiltered(
                SALES_DEPARTMENT,
                filter.status(),
                filter.createdFromInclusive(ZONE),
                filter.createdToExclusive(ZONE)
        );

        return requests.stream()
                .map(request -> dtoMapper.toListItem(
                        request,
                        importRequestRepository.countItemsByRequestId(request.getRequestId())))
                .toList();
    }

    @Override
    public Optional<ImportRequestTrackingDetailDto> getRequestDetail(String requestId) {
        authService.requireRole(UserRole.SALES);
        String id = requireRequestId(requestId);

        return importRequestRepository.findByIdWithItems(id)
                .filter(request -> SALES_DEPARTMENT.equals(request.getDepartment()))
                .map(this::toDetail);
    }

    private ImportRequestTrackingDetailDto toDetail(ImportRequest request) {
        if (request.getStatus() != RequestStatus.DA_TACH_DON) {
            return dtoMapper.toDetail(request, List.of());
        }

        return dtoMapper.toDetail(
                request,
                purchaseOrderRepository.findByRequestId(request.getRequestId())
        );
    }

    private static String requireRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Mã yêu cầu không được để trống.");
        }
        return requestId.trim();
    }
}
