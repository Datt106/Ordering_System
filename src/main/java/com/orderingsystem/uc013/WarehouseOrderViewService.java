package com.orderingsystem.uc013;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc013.controller.WarehouseOrderViewController;

/** @deprecated Dùng WarehouseOrderViewController (BCE). */
@Deprecated
public class WarehouseOrderViewService extends WarehouseOrderViewController {

    public WarehouseOrderViewService() {
        super();
    }

    public WarehouseOrderViewService(AuthService authService, PurchaseOrderRepository purchaseOrderRepository) {
        super(authService, purchaseOrderRepository);
    }
}
