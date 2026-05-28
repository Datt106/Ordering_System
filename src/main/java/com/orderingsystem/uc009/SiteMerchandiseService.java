package com.orderingsystem.uc009;

import com.orderingsystem.uc009.controller.SiteMchController;
import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.Session;
import com.orderingsystem.infrastructure.database.MerchandiseCatalogRepository;
import com.orderingsystem.infrastructure.database.SiteMerchandiseRepository;
import com.orderingsystem.infrastructure.database.SiteRepository;

/** @deprecated Dùng SiteMchController (BCE). */
@Deprecated
public class SiteMerchandiseService extends SiteMchController {

    public SiteMerchandiseService() {
        super();
    }

    public SiteMerchandiseService(AuthService authService,
            SiteRepository siteRepository,
            SiteMerchandiseRepository siteMerchandiseRepository,
            MerchandiseCatalogRepository merchandiseCatalogRepository) {
        super(authService, siteRepository, siteMerchandiseRepository, merchandiseCatalogRepository);
    }

}
