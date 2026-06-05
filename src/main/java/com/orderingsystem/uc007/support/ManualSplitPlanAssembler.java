package com.orderingsystem.uc007.support;

import com.orderingsystem.uc007.boundary.dto.ManualSplitLineInput;
import com.orderingsystem.uc007.boundary.dto.MerchandiseSplitPlanDto;
import com.orderingsystem.uc007.boundary.dto.OrderSplitLineDto;
import com.orderingsystem.uc007.boundary.dto.OrderSplitResultDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ManualSplitPlanAssembler {

    private ManualSplitPlanAssembler() {
    }

    public static OrderSplitResultDto build(SplitPlanContext context, List<ManualSplitLineInput> lines) {
        Map<String, List<OrderSplitLineDto>> grouped = new LinkedHashMap<>();
        for (ManualSplitLineInput line : lines) {
            String merchandiseCode = line.merchandiseCode().trim();
            MerchandiseDemand demand = context.demands().get(merchandiseCode);
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
        for (MerchandiseDemand demand : context.demands().values()) {
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
}
