package com.orderingsystem.uc014;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc014.controller.WarehouseReconcileController;

/** @deprecated Dùng WarehouseReconcileController (BCE). */
@Deprecated
public class WarehouseReconcileService extends WarehouseReconcileController {

    public WarehouseReconcileService() {
        super();
    }

    public WarehouseReconcileService(AuthService authService, PurchaseOrderRepository purchaseOrderRepository) {
        super(authService, purchaseOrderRepository);
    }
}
