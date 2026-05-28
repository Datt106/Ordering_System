package com.orderingsystem.uc012;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc012.controller.SiteOrderConfirmController;

/** @deprecated Dùng SiteOrderConfirmController (BCE). */
@Deprecated
public class SiteOrderConfirmService extends SiteOrderConfirmController {

    public SiteOrderConfirmService() {
        super();
    }

    public SiteOrderConfirmService(AuthService authService, PurchaseOrderRepository purchaseOrderRepository) {
        super(authService, purchaseOrderRepository);
    }
}
