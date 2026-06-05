package com.orderingsystem.uc007.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.RequestStatusEvaluator;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.InventoryQueryRepository;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.infrastructure.database.SiteRepository;
import com.orderingsystem.uc007.boundary.dto.ManualSplitLineInput;
import com.orderingsystem.uc007.boundary.dto.ManualSplitValidationResultDto;
import com.orderingsystem.uc007.boundary.dto.OrderSplitResultDto;
import com.orderingsystem.uc007.subsystem.AllocationFacade;
import com.orderingsystem.uc007.subsystem.AllocationTransaction;
import com.orderingsystem.uc007.subsystem.IAllocationSystem;
import com.orderingsystem.uc007.support.ItemShortageMarker;
import com.orderingsystem.uc007.support.ManualSplitPlanAssembler;
import com.orderingsystem.uc007.support.PurchaseOrderPersister;
import com.orderingsystem.uc007.support.SplitPlanContext;
import com.orderingsystem.uc007.support.SplitPlanContextLoader;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * UC007 — Tách đơn theo <strong>một yêu cầu</strong> ({@code requestId}).
 * Control mỏng: auth, load context, gọi subsystem, persist.
 */
public class OrderSplitController {

    private final AuthService authService;
    private final ImportRequestRepository importRequestRepository;
    private final SplitPlanContextLoader contextLoader;
    private final IAllocationSystem allocationSystem;
    private final PurchaseOrderPersister purchaseOrderPersister;

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
        this(
                authService,
                importRequestRepository,
                new SplitPlanContextLoader(importRequestRepository, inventoryQueryRepository, siteRepository),
                new AllocationFacade(new AllocationTransaction(new ItemShortageMarker(importRequestRepository))),
                new PurchaseOrderPersister(purchaseOrderRepository)
        );
    }

    OrderSplitController(
            AuthService authService,
            ImportRequestRepository importRequestRepository,
            SplitPlanContextLoader contextLoader,
            IAllocationSystem allocationSystem,
            PurchaseOrderPersister purchaseOrderPersister
    ) {
        this.authService = authService;
        this.importRequestRepository = importRequestRepository;
        this.contextLoader = contextLoader;
        this.allocationSystem = allocationSystem;
        this.purchaseOrderPersister = purchaseOrderPersister;
    }

    public OrderSplitResultDto previewSplit(String requestId, LocalDate calculationStartDate) {
        authService.requireRole(UserRole.OVERSEAS);
        SplitPlanContext context = contextLoader.load(requireRequestId(requestId), requireStartDate(calculationStartDate));
        OrderSplitResultDto result = allocationSystem.calculateSplitPlan(context, false);
        RequestStatusEvaluator.markErrorIfNoMerchandiseCanBeSplit(
                importRequestRepository, context.requestId(), context.inventoryReady(), result.plans());
        return result;
    }

    public OrderSplitResultDto confirmSplit(String requestId, LocalDate calculationStartDate) {
        authService.requireRole(UserRole.OVERSEAS);
        String id = requireRequestId(requestId);
        LocalDate startDate = requireStartDate(calculationStartDate);
        SplitPlanContext context = contextLoader.load(id, startDate);
        ensureInventoryReadyForConfirm(context);
        OrderSplitResultDto preview = allocationSystem.calculateSplitPlan(context, true);
        if (!preview.readyToConfirm()) {
            throw new IllegalStateException(
                    "Không thể xác nhận: còn truy vấn tồn kho chưa phản hồi hoặc mặt hàng lỗi.");
        }
        if (!preview.allMerchandiseSucceeded()) {
            throw new IllegalStateException("Không thể xác nhận: còn mặt hàng không đủ hàng hoặc không đáp ứng ngày nhận.");
        }
        purchaseOrderPersister.persist(id, preview.allLines());
        return preview;
    }

    public ManualSplitValidationResultDto validateManualSplit(
            String requestId,
            LocalDate calculationStartDate,
            List<ManualSplitLineInput> lines
    ) {
        authService.requireRole(UserRole.OVERSEAS);
        SplitPlanContext context = contextLoader.load(requireRequestId(requestId), requireStartDate(calculationStartDate));
        List<String> errors = allocationSystem.validateManualPlan(context, lines);
        if (!errors.isEmpty()) {
            return ManualSplitValidationResultDto.invalid(errors);
        }
        return ManualSplitValidationResultDto.ok(ManualSplitPlanAssembler.build(context, lines));
    }

    public OrderSplitResultDto confirmManualSplit(
            String requestId,
            LocalDate calculationStartDate,
            List<ManualSplitLineInput> lines
    ) {
        authService.requireRole(UserRole.OVERSEAS);
        ManualSplitValidationResultDto validation = validateManualSplit(requestId, calculationStartDate, lines);
        if (!validation.valid()) {
            throw new IllegalStateException(String.join("\n", validation.errors()));
        }
        purchaseOrderPersister.persist(requireRequestId(requestId), validation.preview().allLines());
        return validation.preview();
    }

    private static void ensureInventoryReadyForConfirm(SplitPlanContext context) {
        if (context.inventoryReady()) {
            return;
        }
        throw new IllegalStateException(
                "Chưa đủ phản hồi tồn kho (UC006/UC011). Không thể xác nhận phương án tự động.");
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
}
