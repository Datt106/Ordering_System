package com.orderingsystem.uc002;

import com.orderingsystem.uc002.controller.RequestCreateController;
import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.Session;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.MerchandiseCatalogRepository;

/** @deprecated Dùng RequestCreateController (BCE). */
@Deprecated
public class ImportRequestService extends RequestCreateController {

    public ImportRequestService() {
        super();
    }

    public ImportRequestService(AuthService authService,
            ImportRequestRepository importRequestRepository,
            MerchandiseCatalogRepository merchandiseCatalogRepository) {
        super(authService, importRequestRepository, merchandiseCatalogRepository);
    }

}
