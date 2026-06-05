package com.orderingsystem.uc007.subsystem;

import com.orderingsystem.uc007.boundary.dto.ManualSplitLineInput;
import com.orderingsystem.uc007.boundary.dto.OrderSplitResultDto;
import com.orderingsystem.uc007.support.SplitPlanContext;

import java.util.List;

public interface IAllocationSystem {

    OrderSplitResultDto calculateSplitPlan(SplitPlanContext context, boolean forConfirm);

    List<String> validateManualPlan(SplitPlanContext context, List<ManualSplitLineInput> lines);
}
