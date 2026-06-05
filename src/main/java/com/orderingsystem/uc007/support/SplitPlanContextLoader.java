package com.orderingsystem.uc007.support;

import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.InventoryQuery;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.core.domain.Site;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.InventoryQueryRepository;
import com.orderingsystem.infrastructure.database.SiteRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class SplitPlanContextLoader {

    private final ImportRequestRepository importRequestRepository;
    private final InventoryQueryRepository inventoryQueryRepository;
    private final SiteRepository siteRepository;

    public SplitPlanContextLoader(
            ImportRequestRepository importRequestRepository,
            InventoryQueryRepository inventoryQueryRepository,
            SiteRepository siteRepository
    ) {
        this.importRequestRepository = importRequestRepository;
        this.inventoryQueryRepository = inventoryQueryRepository;
        this.siteRepository = siteRepository;
    }

    public SplitPlanContext load(String requestId, LocalDate startDate) {
        ImportRequest request = importRequestRepository.findByIdWithItems(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Yêu cầu không tồn tại: " + requestId));
        if (request.getStatus() != RequestStatus.DANG_XU_LY) {
            throw new IllegalStateException(
                    "Chỉ tách đơn khi yêu cầu ở trạng thái Đang xử lý. Hiện tại: " + request.getStatus());
        }

        long pending = inventoryQueryRepository.countByRequestId(requestId)
                - inventoryQueryRepository.countRespondedByRequestId(requestId);
        boolean inventoryReady = pending == 0 && inventoryQueryRepository.countByRequestId(requestId) > 0;

        Map<String, MerchandiseDemand> demands = DemandAggregator.aggregate(request.getItems());
        Map<String, Site> sitesByCode = new HashMap<>();
        Map<String, Map<String, StockSnapshot>> stockBySiteMerchandise = new HashMap<>();
        for (InventoryQuery row : inventoryQueryRepository.findByRequestId(requestId)) {
            siteRepository.findByCode(row.getSiteCode()).ifPresent(site -> sitesByCode.put(site.getSiteCode(), site));
            stockBySiteMerchandise
                    .computeIfAbsent(row.getSiteCode(), k -> new HashMap<>())
                    .put(row.getMerchandiseCode(), new StockSnapshot(
                            row.getInStockQuantity(),
                            row.getUnit(),
                            row.getRespondedAt() != null
                    ));
        }

        return new SplitPlanContext(requestId, startDate, inventoryReady, demands, sitesByCode, stockBySiteMerchandise);
    }
}
