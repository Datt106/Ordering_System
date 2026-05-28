package com.orderingsystem.uc004;

import com.orderingsystem.uc004.controller.SiteMasterController;
import com.orderingsystem.auth.AuthService;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.infrastructure.database.SiteMerchandiseRepository;
import com.orderingsystem.infrastructure.database.SiteRepository;

/** @deprecated Dùng SiteMasterController (BCE). */
@Deprecated
public class SiteMasterService extends SiteMasterController {

    public SiteMasterService() {
        super();
    }

    public SiteMasterService(AuthService authService,
            SiteRepository siteRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            SiteMerchandiseRepository siteMerchandiseRepository) {
        super(authService, siteRepository, purchaseOrderRepository, siteMerchandiseRepository);
    }

}
