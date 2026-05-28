package com.orderingsystem.fx.app;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.uc001.controller.CatalogController;
import com.orderingsystem.uc002.controller.RequestCreateController;
import com.orderingsystem.uc003.controller.RequestTrackController;
import com.orderingsystem.uc004.controller.SiteMasterController;
import com.orderingsystem.uc005.controller.RequestAcceptController;
import com.orderingsystem.uc006.controller.StockQueryController;
import com.orderingsystem.uc007.controller.OrderSplitController;
import com.orderingsystem.uc008.controller.OrderDispatchController;
import com.orderingsystem.uc009.controller.SiteMchController;
import com.orderingsystem.uc010.controller.SiteShipController;
import com.orderingsystem.uc011.controller.StockReplyController;

/**
 * Composition root: UI (boundary) gọi controller từng UC — không {@code new} repository trong JavaFX controller.
 */
public final class AppContext {

    private final AuthService authService;
    private final CatalogController uc001;
    private final RequestCreateController uc002;
    private final RequestTrackController uc003;
    private final SiteMasterController uc004;
    private final RequestAcceptController uc005;
    private final StockQueryController uc006;
    private final OrderSplitController uc007;
    private final OrderDispatchController uc008;
    private final SiteMchController uc009;
    private final SiteShipController uc010;
    private final StockReplyController uc011;

    public AppContext() {
        this.authService = new AuthService();
        this.uc001 = new CatalogController();
        this.uc002 = new RequestCreateController();
        this.uc003 = new RequestTrackController();
        this.uc004 = new SiteMasterController();
        this.uc005 = new RequestAcceptController();
        this.uc006 = new StockQueryController();
        this.uc007 = new OrderSplitController();
        this.uc008 = new OrderDispatchController();
        this.uc009 = new SiteMchController();
        this.uc010 = new SiteShipController();
        this.uc011 = new StockReplyController();
    }

    public AuthService auth() {
        return authService;
    }

    public CatalogController uc001() {
        return uc001;
    }

    public RequestCreateController uc002() {
        return uc002;
    }

    public RequestTrackController uc003() {
        return uc003;
    }

    public SiteMasterController uc004() {
        return uc004;
    }

    public RequestAcceptController uc005() {
        return uc005;
    }

    public StockQueryController uc006() {
        return uc006;
    }

    public OrderSplitController uc007() {
        return uc007;
    }

    public OrderDispatchController uc008() {
        return uc008;
    }

    public SiteMchController uc009() {
        return uc009;
    }

    public SiteShipController uc010() {
        return uc010;
    }

    public StockReplyController uc011() {
        return uc011;
    }
}
