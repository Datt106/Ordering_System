package com.orderingsystem.fx.app;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.uc001.StandardMerchandiseCatalogService;
import com.orderingsystem.uc002.ImportRequestService;
import com.orderingsystem.uc003.ImportRequestTrackingService;
import com.orderingsystem.uc004.SiteMasterService;
import com.orderingsystem.uc005.ImportRequestAcceptanceService;
import com.orderingsystem.uc006.InventoryQueryService;
import com.orderingsystem.uc009.SiteMerchandiseService;
import com.orderingsystem.uc010.SiteShippingService;
import com.orderingsystem.uc007.OrderSplitService;
import com.orderingsystem.uc011.SiteInventoryResponseService;

/**
 * Composition root (DIP): UI chỉ phụ thuộc một điểm cấp phụ thuộc, không tự {@code new} service trong controller.
 */
public final class AppContext {

    private final AuthService authService;
    private final StandardMerchandiseCatalogService catalogService;
    private final ImportRequestService importRequestService;
    private final ImportRequestTrackingService trackingService;
    private final SiteMasterService siteMasterService;
    private final ImportRequestAcceptanceService acceptanceService;
    private final InventoryQueryService inventoryQueryService;
    private final SiteMerchandiseService siteMerchandiseService;
    private final SiteShippingService siteShippingService;
    private final SiteInventoryResponseService siteInventoryResponseService;
    private final OrderSplitService orderSplitService;

    public AppContext() {
        this.authService = new AuthService();
        this.catalogService = new StandardMerchandiseCatalogService();
        this.importRequestService = new ImportRequestService();
        this.trackingService = new ImportRequestTrackingService();
        this.siteMasterService = new SiteMasterService();
        this.acceptanceService = new ImportRequestAcceptanceService();
        this.inventoryQueryService = new InventoryQueryService();
        this.siteMerchandiseService = new SiteMerchandiseService();
        this.siteShippingService = new SiteShippingService();
        this.siteInventoryResponseService = new SiteInventoryResponseService();
        this.orderSplitService = new OrderSplitService();
    }

    public AuthService auth() {
        return authService;
    }

    public StandardMerchandiseCatalogService catalog() {
        return catalogService;
    }

    public ImportRequestService importRequests() {
        return importRequestService;
    }

    public ImportRequestTrackingService tracking() {
        return trackingService;
    }

    public SiteMasterService sites() {
        return siteMasterService;
    }

    public ImportRequestAcceptanceService acceptance() {
        return acceptanceService;
    }

    public InventoryQueryService inventoryQueries() {
        return inventoryQueryService;
    }

    public SiteMerchandiseService siteMerchandise() {
        return siteMerchandiseService;
    }

    public SiteShippingService siteShipping() {
        return siteShippingService;
    }

    public SiteInventoryResponseService siteInventoryResponses() {
        return siteInventoryResponseService;
    }

    public OrderSplitService orderSplit() {
        return orderSplitService;
    }
}
