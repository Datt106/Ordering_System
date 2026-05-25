package com.orderingsystem.uc007;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.domain.inventory.InventoryQuery;
import com.orderingsystem.domain.order.PurchaseOrder;
import com.orderingsystem.domain.request.ImportRequest;
import com.orderingsystem.domain.request.ImportRequestItem;
import com.orderingsystem.domain.request.ItemStatus;
import com.orderingsystem.domain.request.RequestStatus;
import com.orderingsystem.domain.site.Site;
import com.orderingsystem.infrastructure.repository.ImportRequestRepository;
import com.orderingsystem.infrastructure.repository.InventoryQueryRepository;
import com.orderingsystem.infrastructure.repository.PurchaseOrderRepository;
import com.orderingsystem.infrastructure.repository.SiteRepository;
import com.orderingsystem.uc007.dto.MerchandiseSplitPlanDto;
import com.orderingsystem.uc007.dto.OrderSplitLineDto;
import com.orderingsystem.uc007.dto.OrderSplitResultDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * UC007 — Tách đơn theo <strong>một yêu cầu</strong> ({@code requestId}).
 * Mỗi <strong>mã hàng</strong> trong REQ chạy thuật toán phân bổ một lần
 * (các dòng trùng mã trong cùng REQ được cộng Q, D = ngày nhận sớm nhất).
 * Không gộp nhiều REQ.
 */
public class OrderSplitService {

    private final AuthService authService;
    private final ImportRequestRepository importRequestRepository;
    private final InventoryQueryRepository inventoryQueryRepository;
    private final SiteRepository siteRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public OrderSplitService() {
        this(
                new AuthService(),
                new ImportRequestRepository(),
                new InventoryQueryRepository(),
                new SiteRepository(),
                new PurchaseOrderRepository()
        );
    }

    public OrderSplitService(
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
        authService.requireRole(com.orderingsystem.domain.auth.UserRole.OVERSEAS);
        return buildSplitPlan(requireRequestId(requestId), requireStartDate(calculationStartDate), false);
    }

    /** Xác nhận tách đơn — lưu đơn con trạng thái Chờ gửi. */
    public OrderSplitResultDto confirmSplit(String requestId, LocalDate calculationStartDate) {
        authService.requireRole(com.orderingsystem.domain.auth.UserRole.OVERSEAS);
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

        purchaseOrderRepository.deleteByRequestId(id);
        List<PurchaseOrder> orders = new ArrayList<>();
        int seq = 1;
        for (OrderSplitLineDto line : preview.allLines()) {
            orders.add(new PurchaseOrder(
                    buildOrderId(id, seq++),
                    id,
                    line.siteCode(),
                    line.merchandiseCode(),
                    line.quantity(),
                    line.unit(),
                    line.deliveryMeans()
            ));
        }
        purchaseOrderRepository.saveAll(orders);
        return preview;
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
