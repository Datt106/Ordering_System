package com.orderingsystem.uc007.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.RequestStatusEvaluator;
import com.orderingsystem.core.domain.InventoryQuery;
import com.orderingsystem.core.domain.PurchaseOrder;
import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.ImportRequestItem;
import com.orderingsystem.core.domain.ItemStatus;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.core.domain.Site;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.InventoryQueryRepository;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.infrastructure.database.SiteRepository;
import com.orderingsystem.uc007.boundary.dto.ManualSplitLineInput;
import com.orderingsystem.uc007.boundary.dto.ManualSplitValidationResultDto;
import com.orderingsystem.uc007.boundary.dto.MerchandiseSplitPlanDto;
import com.orderingsystem.uc007.boundary.dto.OrderSplitLineDto;
import com.orderingsystem.uc007.boundary.dto.OrderSplitResultDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * UC007 — Tách đơn theo <strong>một yêu cầu</strong> ({@code requestId}).
 * Mỗi <strong>mã hàng</strong> trong REQ chạy thuật toán phân bổ một lần
 * (các dòng trùng mã trong cùng REQ được cộng Q, D = ngày nhận sớm nhất).
 * Không gộp nhiều REQ.
 */
public class OrderSplitController {

    private final AuthService authService;
    private final ImportRequestRepository importRequestRepository;
    private final InventoryQueryRepository inventoryQueryRepository;
    private final SiteRepository siteRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public OrderSplitController() {
        this(
                new AuthService(),
                new ImportRequestRepository(),
                new InventoryQueryRepository(),
                new SiteRepository(),
                new PurchaseOrderRepository()
        );
    }

    public OrderSplitController(
            AuthService authService,
            ImportRequestRepository importRequestRepository,
            InventoryQueryRepository inventoryQueryRepository,
            SiteRepository siteRepository,
            PurchaseOrderRepository purchaseOrderRepository
    ) {
        this.authService = authService;
        this.importRequestRepository = importRequestRepository;
        this.inventoryQueryRepository = inventoryQueryRepository;
        this.siteRepository = siteRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    /** Xem trước phương án tách đơn (chưa lưu đơn con). */
    public OrderSplitResultDto previewSplit(String requestId, LocalDate calculationStartDate) {
        authService.requireRole(com.orderingsystem.core.domain.UserRole.OVERSEAS);
        return buildSplitPlan(requireRequestId(requestId), requireStartDate(calculationStartDate), false);
    }

    /** Xác nhận tách đơn theo phương án tự động — lưu đơn con trạng thái Chờ gửi. */
    public OrderSplitResultDto confirmSplit(String requestId, LocalDate calculationStartDate) {
        authService.requireRole(com.orderingsystem.core.domain.UserRole.OVERSEAS);
        String id = requireRequestId(requestId);
        LocalDate startDate = requireStartDate(calculationStartDate);
        OrderSplitResultDto preview = buildSplitPlan(id, startDate, true);
        if (!preview.readyToConfirm()) {
            throw new IllegalStateException(
                    "Không thể xác nhận: còn truy vấn tồn kho chưa phản hồi hoặc mặt hàng lỗi.");
        }
        if (!preview.allMerchandiseSucceeded()) {
            throw new IllegalStateException("Không thể xác nhận: còn mặt hàng không đủ hàng hoặc không đáp ứng ngày nhận.");
        }
        persistPurchaseOrders(id, preview.allLines());
        return preview;
    }

    /** Kiểm tra phương án do nhân viên chỉnh tay trước khi xác nhận (FR-06.9). */
    public ManualSplitValidationResultDto validateManualSplit(
            String requestId,
            LocalDate calculationStartDate,
            List<ManualSplitLineInput> lines
    ) {
        authService.requireRole(com.orderingsystem.core.domain.UserRole.OVERSEAS);
        SplitPlanContext context = loadSplitPlanContext(requireRequestId(requestId), requireStartDate(calculationStartDate));
        List<String> errors = ManualSplitPlanValidator.validate(
                context.requestId(),
                context.startDate(),
                context.inventoryReady(),
                context.demandSnapshots(),
                context.sitesByCode(),
                context.stockBySiteMerchandise(),
                lines != null ? lines : List.of()
        );
        if (!errors.isEmpty()) {
            return ManualSplitValidationResultDto.invalid(errors);
        }
        return ManualSplitValidationResultDto.ok(buildResultFromManualLines(context, lines));
    }

    /** Xác nhận phương án đã chỉnh tay sau khi validate. */
    public OrderSplitResultDto confirmManualSplit(
            String requestId,
            LocalDate calculationStartDate,
            List<ManualSplitLineInput> lines
    ) {
        authService.requireRole(com.orderingsystem.core.domain.UserRole.OVERSEAS);
        ManualSplitValidationResultDto validation = validateManualSplit(requestId, calculationStartDate, lines);
        if (!validation.valid()) {
            throw new IllegalStateException(String.join("\n", validation.errors()));
        }
        String id = requireRequestId(requestId);
        persistPurchaseOrders(id, validation.preview().allLines());
        return validation.preview();
    }

    private OrderSplitResultDto buildSplitPlan(
            String requestId,
            LocalDate startDate,
            boolean forConfirm
    ) {
        ImportRequest request = importRequestRepository.findByIdWithItems(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Yêu cầu không tồn tại: " + requestId));
        if (request.getStatus() != RequestStatus.DANG_XU_LY) {
            throw new IllegalStateException(
                    "Chỉ tách đơn khi yêu cầu ở trạng thái Đang xử lý. Hiện tại: " + request.getStatus());
        }

        long pending = inventoryQueryRepository.countByRequestId(requestId)
                - inventoryQueryRepository.countRespondedByRequestId(requestId);
        boolean inventoryReady = pending == 0 && inventoryQueryRepository.countByRequestId(requestId) > 0;
        if (!inventoryReady && forConfirm) {
            throw new IllegalStateException(
                    "Chưa đủ phản hồi tồn kho (UC006/UC011). Còn " + pending + " dòng chờ Site.");
        }

        Map<String, MerchandiseDemand> demands = aggregateDemands(request.getItems());
        List<MerchandiseSplitPlanDto> plans = new ArrayList<>();
        List<OrderSplitLineDto> allLines = new ArrayList<>();

        for (MerchandiseDemand demand : demands.values()) {
            MerchandiseSplitPlanDto plan = planForMerchandise(
                    requestId,
                    demand,
                    startDate,
                    inventoryReady,
                    forConfirm
            );
            plans.add(plan);
            if (plan.success()) {
                allLines.addAll(plan.lines());
            }
        }

        RequestStatusEvaluator.markErrorIfNoMerchandiseCanBeSplit(
                importRequestRepository, requestId, inventoryReady, plans);

        return new OrderSplitResultDto(
                requestId,
                startDate,
                plans,
                allLines,
                inventoryReady
        );
    }

    private MerchandiseSplitPlanDto planForMerchandise(
            String requestId,
            MerchandiseDemand demand,
            LocalDate startDate,
            boolean inventoryReady,
            boolean forConfirm
    ) {
        if (demand.skippedNoSite()) {
            return new MerchandiseSplitPlanDto(
                    demand.merchandiseCode(),
                    demand.quantityNeeded(),
                    demand.targetDate(),
                    List.of(),
                    "Mặt hàng đã đánh dấu không có Site kinh doanh (UC006).",
                    0
            );
        }
        if (!inventoryReady) {
            return new MerchandiseSplitPlanDto(
                    demand.merchandiseCode(),
                    demand.quantityNeeded(),
                    demand.targetDate(),
                    List.of(),
                    "Chưa đủ phản hồi tồn kho từ Site.",
                    0
            );
        }

        List<InventoryQuery> stockRows = inventoryQueryRepository.findByRequestId(requestId).stream()
                .filter(q -> q.getMerchandiseCode().equals(demand.merchandiseCode()))
                .filter(q -> q.getRespondedAt() != null)
                .toList();

        List<MerchandiseAllocationEngine.SitePool> shipPools = new ArrayList<>();
        List<MerchandiseAllocationEngine.SitePool> airPools = new ArrayList<>();

        for (InventoryQuery row : stockRows) {
            if (row.getInStockQuantity() <= 0) {
                continue;
            }
            Site site = siteRepository.findByCode(row.getSiteCode()).orElse(null);
            if (site == null || site.getShipDays() == null || site.getAirDays() == null) {
                continue;
            }
            LocalDate etaShip = startDate.plusDays(site.getShipDays());
            LocalDate etaAir = startDate.plusDays(site.getAirDays());
            var pool = new MerchandiseAllocationEngine.SitePool(row.getSiteCode(), row.getInStockQuantity());
            if (!etaShip.isAfter(demand.targetDate())) {
                shipPools.add(pool);
            }
            if (!etaAir.isAfter(demand.targetDate())) {
                airPools.add(pool);
            }
        }

        MerchandiseAllocationEngine.Plan plan = MerchandiseAllocationEngine.allocate(
                demand.quantityNeeded(),
                shipPools,
                airPools
        );

        if (plan.hasNoEligibleSite()) {
            markItemsShortage(demand, forConfirm);
            return new MerchandiseSplitPlanDto(
                    demand.merchandiseCode(),
                    demand.quantityNeeded(),
                    demand.targetDate(),
                    List.of(),
                    "Không có Site đáp ứng ngày nhận mong muốn.",
                    0
            );
        }
        if (plan.shortfall() > 0) {
            markItemsShortage(demand, forConfirm);
            return new MerchandiseSplitPlanDto(
                    demand.merchandiseCode(),
                    demand.quantityNeeded(),
                    demand.targetDate(),
                    List.of(),
                    "Không đủ hàng cho " + demand.merchandiseCode(),
                    plan.shortfall()
            );
        }

        List<OrderSplitLineDto> lines = plan.lines().stream()
                .map(line -> OrderSplitLineDto.of(
                        line.siteCode(),
                        demand.merchandiseCode(),
                        line.quantity(),
                        demand.unit(),
                        line.deliveryMeans()))
                .toList();
        return new MerchandiseSplitPlanDto(
                demand.merchandiseCode(),
                demand.quantityNeeded(),
                demand.targetDate(),
                lines,
                null,
                0
        );
    }

    private void markItemsShortage(MerchandiseDemand demand, boolean forConfirm) {
        if (!forConfirm) {
            return;
        }
        for (Long itemId : demand.itemIds()) {
            importRequestRepository.updateItemStatus(itemId, ItemStatus.LOI_KHONG_DU_HANG);
        }
    }

    private void persistPurchaseOrders(String requestId, List<OrderSplitLineDto> lines) {
        purchaseOrderRepository.deleteByRequestId(requestId);
        List<PurchaseOrder> orders = new ArrayList<>();
        int seq = 1;
        for (OrderSplitLineDto line : lines) {
            orders.add(new PurchaseOrder(
                    buildOrderId(requestId, seq++),
                    requestId,
                    line.siteCode(),
                    line.merchandiseCode(),
                    line.quantity(),
                    line.unit(),
                    line.deliveryMeans()
            ));
        }
        purchaseOrderRepository.saveAll(orders);
    }

    private SplitPlanContext loadSplitPlanContext(String requestId, LocalDate startDate) {
        ImportRequest request = importRequestRepository.findByIdWithItems(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Yêu cầu không tồn tại: " + requestId));
        if (request.getStatus() != RequestStatus.DANG_XU_LY) {
            throw new IllegalStateException(
                    "Chỉ tách đơn khi yêu cầu ở trạng thái Đang xử lý. Hiện tại: " + request.getStatus());
        }

        long pending = inventoryQueryRepository.countByRequestId(requestId)
                - inventoryQueryRepository.countRespondedByRequestId(requestId);
        boolean inventoryReady = pending == 0 && inventoryQueryRepository.countByRequestId(requestId) > 0;

        Map<String, MerchandiseDemand> demands = aggregateDemands(request.getItems());
        Map<String, ManualSplitPlanValidator.DemandSnapshot> demandSnapshots = demands.values().stream()
                .collect(Collectors.toMap(
                        MerchandiseDemand::merchandiseCode,
                        d -> new ManualSplitPlanValidator.DemandSnapshot(
                                d.merchandiseCode(),
                                d.quantityNeeded(),
                                d.targetDate(),
                                d.unit(),
                                d.skippedNoSite()
                        ),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, Site> sitesByCode = new HashMap<>();
        Map<String, Map<String, ManualSplitPlanValidator.StockSnapshot>> stockBySiteMerchandise = new HashMap<>();
        for (InventoryQuery row : inventoryQueryRepository.findByRequestId(requestId)) {
            siteRepository.findByCode(row.getSiteCode()).ifPresent(site -> sitesByCode.put(site.getSiteCode(), site));
            stockBySiteMerchandise
                    .computeIfAbsent(row.getSiteCode(), k -> new HashMap<>())
                    .put(row.getMerchandiseCode(), new ManualSplitPlanValidator.StockSnapshot(
                            row.getInStockQuantity(),
                            row.getUnit(),
                            row.getRespondedAt() != null
                    ));
        }

        return new SplitPlanContext(requestId, startDate, inventoryReady, demandSnapshots, sitesByCode, stockBySiteMerchandise);
    }

    private OrderSplitResultDto buildResultFromManualLines(SplitPlanContext context, List<ManualSplitLineInput> lines) {
        Map<String, List<OrderSplitLineDto>> grouped = new LinkedHashMap<>();
        for (ManualSplitLineInput line : lines) {
            String merchandiseCode = line.merchandiseCode().trim();
            ManualSplitPlanValidator.DemandSnapshot demand = context.demandSnapshots().get(merchandiseCode);
            grouped.computeIfAbsent(merchandiseCode, k -> new ArrayList<>())
                    .add(OrderSplitLineDto.of(
                            line.siteCode().trim(),
                            merchandiseCode,
                            line.quantity(),
                            demand.unit(),
                            line.deliveryMeans()
                    ));
        }

        List<MerchandiseSplitPlanDto> plans = new ArrayList<>();
        List<OrderSplitLineDto> allLines = new ArrayList<>();
        for (ManualSplitPlanValidator.DemandSnapshot demand : context.demandSnapshots().values()) {
            List<OrderSplitLineDto> planLines = grouped.getOrDefault(demand.merchandiseCode(), List.of());
            if (demand.skippedNoSite()) {
                plans.add(new MerchandiseSplitPlanDto(
                        demand.merchandiseCode(),
                        demand.quantityNeeded(),
                        demand.targetDate(),
                        List.of(),
                        "Mặt hàng đã đánh dấu không có Site kinh doanh (UC006).",
                        0
                ));
            } else {
                plans.add(new MerchandiseSplitPlanDto(
                        demand.merchandiseCode(),
                        demand.quantityNeeded(),
                        demand.targetDate(),
                        planLines,
                        null,
                        0
                ));
                allLines.addAll(planLines);
            }
        }

        return new OrderSplitResultDto(
                context.requestId(),
                context.startDate(),
                plans,
                allLines,
                context.inventoryReady()
        );
    }

    private record SplitPlanContext(
            String requestId,
            LocalDate startDate,
            boolean inventoryReady,
            Map<String, ManualSplitPlanValidator.DemandSnapshot> demandSnapshots,
            Map<String, Site> sitesByCode,
            Map<String, Map<String, ManualSplitPlanValidator.StockSnapshot>> stockBySiteMerchandise
    ) {
    }

    private static Map<String, MerchandiseDemand> aggregateDemands(List<ImportRequestItem> items) {
        Map<String, MerchandiseDemand> map = new LinkedHashMap<>();
        for (ImportRequestItem item : items) {
            String code = item.getMerchandiseCode();
            MerchandiseDemand existing = map.get(code);
            if (existing == null) {
                map.put(code, MerchandiseDemand.from(item));
            } else {
                map.put(code, existing.merge(item));
            }
        }
        return map;
    }

    private static String buildOrderId(String requestId, int sequence) {
        return "PO-" + requestId + "-" + sequence;
    }

    private static String requireRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Mã yêu cầu không được để trống.");
        }
        return requestId.trim();
    }

    private static LocalDate requireStartDate(LocalDate startDate) {
        return Objects.requireNonNull(startDate, "Ngày bắt đầu tính toán không được null.");
    }

    private record MerchandiseDemand(
            String merchandiseCode,
            int quantityNeeded,
            LocalDate targetDate,
            String unit,
            List<Long> itemIds,
            boolean skippedNoSite
    ) {
        static MerchandiseDemand from(ImportRequestItem item) {
            return new MerchandiseDemand(
                    item.getMerchandiseCode(),
                    item.getQuantityOrdered(),
                    item.getDesiredDeliveryDate(),
                    item.getUnit(),
                    List.of(item.getId()),
                    item.getItemStatus() == ItemStatus.KHONG_CO_SITE
            );
        }

        MerchandiseDemand merge(ImportRequestItem item) {
            boolean noSite = skippedNoSite() || item.getItemStatus() == ItemStatus.KHONG_CO_SITE;
            LocalDate earliest = targetDate().isBefore(item.getDesiredDeliveryDate())
                    ? targetDate()
                    : item.getDesiredDeliveryDate();
            List<Long> ids = new ArrayList<>(itemIds());
            ids.add(item.getId());
            return new MerchandiseDemand(
                    merchandiseCode(),
                    quantityNeeded() + item.getQuantityOrdered(),
                    earliest,
                    unit(),
                    ids,
                    noSite
            );
        }
    }
}
