package com.orderingsystem.uc005;

import com.orderingsystem.uc005.controller.RequestAcceptController;
import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.Session;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;

/** @deprecated Dùng RequestAcceptController (BCE). */
@Deprecated
public class ImportRequestAcceptanceService extends RequestAcceptController {

    public ImportRequestAcceptanceService() {
        super();
    }

    public ImportRequestAcceptanceService(AuthService authService, ImportRequestRepository importRequestRepository) {
        super(authService, importRequestRepository);
    }

}
