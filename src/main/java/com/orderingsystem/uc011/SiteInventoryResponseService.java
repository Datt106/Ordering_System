package com.orderingsystem.uc011;

import com.orderingsystem.uc011.controller.StockReplyController;
import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.Session;
import com.orderingsystem.infrastructure.database.InventoryQueryRepository;

/** @deprecated Dùng StockReplyController (BCE). */
@Deprecated
public class SiteInventoryResponseService extends StockReplyController {

    public SiteInventoryResponseService() {
        super();
    }

    public SiteInventoryResponseService(AuthService authService,
            InventoryQueryRepository inventoryQueryRepository) {
        super(authService, inventoryQueryRepository);
    }

}
