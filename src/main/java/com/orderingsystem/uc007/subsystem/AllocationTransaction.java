package com.orderingsystem.uc007.subsystem;

import com.orderingsystem.uc007.boundary.dto.MerchandiseSplitPlanDto;
import com.orderingsystem.uc007.boundary.dto.OrderSplitLineDto;
import com.orderingsystem.uc007.boundary.dto.OrderSplitResultDto;
import com.orderingsystem.uc007.support.ItemShortageMarker;
import com.orderingsystem.uc007.support.MerchandiseDemand;
import com.orderingsystem.uc007.support.SplitPlanContext;

import java.util.ArrayList;
import java.util.List;

public class AllocationTransaction {

    private final ItemShortageMarker itemShortageMarker;

    public AllocationTransaction(ItemShortageMarker itemShortageMarker) {
        this.itemShortageMarker = itemShortageMarker;
    }

    public OrderSplitResultDto execute(SplitPlanContext context, boolean forConfirm) {
        List<MerchandiseSplitPlanDto> plans = new ArrayList<>();
        List<OrderSplitLineDto> allLines = new ArrayList<>();

        for (MerchandiseDemand demand : context.demands().values()) {
            MerchandiseSplitPlanDto plan = planForMerchandise(context, demand, forConfirm);
            plans.add(plan);
            if (plan.success()) {
                allLines.addAll(plan.lines());
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

    private MerchandiseSplitPlanDto planForMerchandise(
            SplitPlanContext context,
            MerchandiseDemand demand,
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
        if (!context.inventoryReady()) {
            return new MerchandiseSplitPlanDto(
                    demand.merchandiseCode(),
                    demand.quantityNeeded(),
                    demand.targetDate(),
                    List.of(),
                    "Chưa đủ phản hồi tồn kho từ Site.",
                    0
            );
        }

        SiteFilter.SitePools pools = SiteFilter.eligiblePools(context, demand);
        MerchandiseAllocationEngine.Plan plan = MerchandiseAllocationEngine.allocate(
                demand.quantityNeeded(),
                pools.shipPools(),
                pools.airPools()
        );

        if (plan.hasNoEligibleSite()) {
            itemShortageMarker.markIfConfirming(demand, forConfirm);
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
            itemShortageMarker.markIfConfirming(demand, forConfirm);
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
}
