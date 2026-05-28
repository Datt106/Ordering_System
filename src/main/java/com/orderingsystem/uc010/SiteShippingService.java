package com.orderingsystem.uc010;

import com.orderingsystem.uc010.controller.SiteShipController;
import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.Session;
import com.orderingsystem.infrastructure.database.SiteRepository;

/** @deprecated Dùng SiteShipController (BCE). */
@Deprecated
public class SiteShippingService extends SiteShipController {

    public SiteShippingService() {
        super();
    }

    public SiteShippingService(AuthService authService, SiteRepository siteRepository) {
        super(authService, siteRepository);
    }

}
