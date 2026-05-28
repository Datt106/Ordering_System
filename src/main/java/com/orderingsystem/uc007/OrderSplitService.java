package com.orderingsystem.uc007;

import com.orderingsystem.uc007.controller.OrderSplitController;
import com.orderingsystem.auth.AuthService;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.InventoryQueryRepository;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.infrastructure.database.SiteRepository;

/** @deprecated Dùng OrderSplitController (BCE). */
@Deprecated
public class OrderSplitService extends OrderSplitController {

    public OrderSplitService() {
        super();
    }

    public OrderSplitService(AuthService authService,
            ImportRequestRepository importRequestRepository,
            InventoryQueryRepository inventoryQueryRepository,
            SiteRepository siteRepository,
            PurchaseOrderRepository purchaseOrderRepository) {
        super(authService, importRequestRepository, inventoryQueryRepository, siteRepository, purchaseOrderRepository);
    }

}
