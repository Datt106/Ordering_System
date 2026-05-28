package com.orderingsystem.uc006;

import com.orderingsystem.uc006.controller.StockQueryController;
import com.orderingsystem.auth.AuthService;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.InventoryQueryRepository;
import com.orderingsystem.infrastructure.database.SiteMerchandiseRepository;
import com.orderingsystem.infrastructure.database.SiteRepository;

/** @deprecated Dùng StockQueryController (BCE). */
@Deprecated
public class InventoryQueryService extends StockQueryController {

    public InventoryQueryService() {
        super();
    }

    public InventoryQueryService(AuthService authService,
            ImportRequestRepository importRequestRepository,
            SiteRepository siteRepository,
            SiteMerchandiseRepository siteMerchandiseRepository,
            InventoryQueryRepository inventoryQueryRepository) {
        super(authService, importRequestRepository, siteRepository, siteMerchandiseRepository, inventoryQueryRepository);
    }

}
