package com.orderingsystem.uc001;

import com.orderingsystem.uc001.controller.CatalogController;
import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.Session;
import com.orderingsystem.infrastructure.database.MerchandiseCatalogRepository;

/** @deprecated Dùng CatalogController (BCE). */
@Deprecated
public class StandardMerchandiseCatalogService extends CatalogController {

    public StandardMerchandiseCatalogService() {
        super();
    }

    public StandardMerchandiseCatalogService(AuthService authService,
            MerchandiseCatalogRepository merchandiseCatalogRepository) {
        super(authService, merchandiseCatalogRepository);
    }

}
