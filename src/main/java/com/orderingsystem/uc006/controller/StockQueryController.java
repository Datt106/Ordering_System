package com.orderingsystem.uc006.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.RequestStatusEvaluator;
import com.orderingsystem.core.domain.InventoryQuery;
import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.ImportRequestItem;
import com.orderingsystem.core.domain.ItemStatus;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.core.domain.ShippingStatus;
import com.orderingsystem.core.domain.Site;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.InventoryQueryRepository;
import com.orderingsystem.infrastructure.database.SiteMerchandiseRepository;
import com.orderingsystem.infrastructure.database.SiteRepository;
import com.orderingsystem.uc006.boundary.dto.InventoryQueryDispatchResultDto;
import com.orderingsystem.uc006.boundary.dto.InventoryQueryDto;
import com.orderingsystem.uc006.boundary.dto.MerchandiseQueryErrorDto;
import com.orderingsystem.uc006.boundary.dto.SiteInventoryQueryGroupDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * UC006 — Overseas gửi truy vấn tồn kho tới các Site (yêu cầu Đang xử lý); Site trả lời qua UC011.
 */
public class StockQueryController {

    private final AuthService authService;
    private final ImportRequestRepository importRequestRepository;
    private final SiteRepository siteRepository;
    private final SiteMerchandiseRepository siteMerchandiseRepository;
    private final InventoryQueryRepository inventoryQueryRepository;

    public StockQueryController() {
        this(
                new AuthService(),
                new ImportRequestRepository(),
                new SiteRepository(),
                new SiteMerchandiseRepository(),
                new InventoryQueryRepository()
        );
    }

    public StockQueryController(
            AuthService authService,
            ImportRequestRepository importRequestRepository,
            SiteRepository siteRepository,
            SiteMerchandiseRepository siteMerchandiseRepository,
            InventoryQueryRepository inventoryQueryRepository
    ) {
        this.authService = authService;
        this.importRequestRepository = importRequestRepository;
        this.siteRepository = siteRepository;
        this.siteMerchandiseRepository = siteMerchandiseRepository;
        this.inventoryQueryRepository = inventoryQueryRepository;
    }

    /**
     * Gửi truy vấn tồn kho: nhóm mặt hàng theo Site, tạo bản ghi chờ phản hồi (respondedAt = null).
     */
    public InventoryQueryDispatchResultDto dispatchInventoryQueries(String requestId) {
        authService.requireRole(com.orderingsystem.core.domain.UserRole.OVERSEAS);
        String id = requireRequestId(requestId);

        ImportRequest request = importRequestRepository.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Yêu cầu không tồn tại: " + id));
        if (request.getStatus() != RequestStatus.DANG_XU_LY) {
            throw new IllegalStateException(
                    "Chỉ truy vấn tồn kho khi yêu cầu ở trạng thái Đang xử lý. Hiện tại: " + request.getStatus());
        }

        // Kiểm tra xem yêu cầu này đã được gửi truy vấn chưa?
        List<InventoryQuery> existingQueries = inventoryQueryRepository.findByRequestId(id);
        if (!existingQueries.isEmpty()) {
            throw new IllegalStateException("Yêu cầu này đã được gửi truy vấn tồn kho từ trước. Không thể gửi lại!");
        }

        RequestStatusEvaluator.clearErrorStatusIfProcessing(importRequestRepository, id);
        for (ImportRequestItem item : request.getItems()) {
            importRequestRepository.updateItemStatus(item.getId(), ItemStatus.OK);
        }

        List<InventoryQuery> created = new ArrayList<>();
        List<MerchandiseQueryErrorDto> errors = new ArrayList<>();

        for (ImportRequestItem item : request.getItems()) {
            Set<String> siteCodes = new LinkedHashSet<>(
                    siteMerchandiseRepository.findSiteCodesByMerchandiseCode(item.getMerchandiseCode()));

            List<String> eligibleSites = siteCodes.stream()
                    .filter(this::isEligibleForInventoryQuery)
                    .toList();

            if (eligibleSites.isEmpty()) {
                importRequestRepository.updateItemStatus(item.getId(), ItemStatus.KHONG_CO_SITE);
                errors.add(new MerchandiseQueryErrorDto(
                        item.getMerchandiseCode(),
                        "Không có Site active đã khai báo vận chuyển kinh doanh mặt hàng này."));
                continue;
            }

            for (String siteCode : eligibleSites) {
                InventoryQuery query = new InventoryQuery(
                        buildQueryId(id, siteCode, item.getMerchandiseCode()),
                        id,
                        siteCode,
                        item.getMerchandiseCode(),
                        0,
                        item.getUnit(),
                        null
                );
                inventoryQueryRepository.save(query);
                created.add(query);
            }
        }

        RequestStatusEvaluator.markErrorIfAllItemsUnfulfillable(importRequestRepository, id);
        return buildDispatchResult(id, created, errors);
    }

    /** Trạng thái truy vấn của một yêu cầu (Overseas). */
    public InventoryQueryDispatchResultDto getInventoryQueryStatus(String requestId) {
        authService.requireRole(com.orderingsystem.core.domain.UserRole.OVERSEAS);
        String id = requireRequestId(requestId);
        List<InventoryQuery> queries = inventoryQueryRepository.findByRequestId(id);
        return buildDispatchResult(id, queries, List.of());
    }

    /** Toàn bộ dòng tồn kho đã ghi cho yêu cầu. */
    public List<InventoryQueryDto> listQueriesByRequest(String requestId) {
        authService.requireRole(com.orderingsystem.core.domain.UserRole.OVERSEAS);
        return inventoryQueryRepository.findByRequestId(requireRequestId(requestId)).stream()
                .map(InventoryQueryDto::from)
                .toList();
    }

    /**
     * FR-05.5 — Site không phản hồi: ghi tồn kho = 0 cho các dòng còn chờ (demo / gọi thủ công).
     */
    public int applyTimeoutAsZeroStock(String requestId) {
        authService.requireRole(com.orderingsystem.core.domain.UserRole.OVERSEAS);
        String id = requireRequestId(requestId);
        List<InventoryQuery> pending = inventoryQueryRepository.findByRequestId(id).stream()
                .filter(InventoryQuery::isPending)
                .toList();
        // Chặn nếu không còn gì để chờ (do đã phản hồi hết hoặc đã bấm ngắt trước đó)
        if (pending.isEmpty()) {
            throw new IllegalStateException("Tất cả các Site đã phản hồi hoặc bạn đã thực hiện ngắt kết nối trước đó. Không thể thao tác lại!");
        }
        for (InventoryQuery query : pending) {
            query.setInStockQuantity(0);
            query.setRespondedAt(java.time.Instant.now());
            inventoryQueryRepository.save(query);
        }
        return pending.size();
    }

    private boolean isEligibleForInventoryQuery(String siteCode) {
        return siteRepository.findByCode(siteCode)
                .filter(site -> site.isActive() && site.getShippingStatus() == ShippingStatus.DA_KHAI_BAO)
                .filter(site -> site.getShipDays() != null && site.getAirDays() != null)
                .isPresent();
    }

    private InventoryQueryDispatchResultDto buildDispatchResult(
            String requestId,
            List<InventoryQuery> queries,
            List<MerchandiseQueryErrorDto> errors
    ) {
        Map<String, List<InventoryQueryDto>> bySite = new LinkedHashMap<>();
        for (InventoryQuery query : queries) {
            bySite.computeIfAbsent(query.getSiteCode(), k -> new ArrayList<>())
                    .add(InventoryQueryDto.from(query));
        }

        List<SiteInventoryQueryGroupDto> groups = new ArrayList<>();
        for (Map.Entry<String, List<InventoryQueryDto>> entry : bySite.entrySet()) {
            Site site = siteRepository.findByCode(entry.getKey()).orElse(null);
            groups.add(new SiteInventoryQueryGroupDto(
                    entry.getKey(),
                    site != null ? site.getSiteName() : entry.getKey(),
                    site != null ? site.getShipDays() : null,
                    site != null ? site.getAirDays() : null,
                    entry.getValue()
            ));
        }

        long pending = queries.stream().filter(InventoryQuery::isPending).count();
        return new InventoryQueryDispatchResultDto(
                requestId,
                queries.size(),
                (int) pending,
                groups,
                errors
        );
    }

    private static String buildQueryId(String requestId, String siteCode, String merchandiseCode) {
        return "IQ-" + requestId + "-" + siteCode + "-" + merchandiseCode;
    }

    private static String requireRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Mã yêu cầu không được để trống.");
        }
        return requestId.trim();
    }
}
