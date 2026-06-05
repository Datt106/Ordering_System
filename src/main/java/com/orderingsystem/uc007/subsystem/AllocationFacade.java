package com.orderingsystem.uc007.subsystem;

import com.orderingsystem.uc007.boundary.dto.ManualSplitLineInput;
import com.orderingsystem.uc007.boundary.dto.OrderSplitResultDto;
import com.orderingsystem.uc007.support.SplitPlanContext;

import java.util.List;

public class AllocationFacade implements IAllocationSystem {

    private final AllocationTransaction allocationTransaction;

    public AllocationFacade(AllocationTransaction allocationTransaction) {
        this.allocationTransaction = allocationTransaction;
    }

    @Override
    public OrderSplitResultDto calculateSplitPlan(SplitPlanContext context, boolean forConfirm) {
        return allocationTransaction.execute(context, forConfirm);
    }

    @Override
    public List<String> validateManualPlan(SplitPlanContext context, List<ManualSplitLineInput> lines) {
        return PlanValidator.validate(context, lines != null ? lines : List.of());
    }
}
