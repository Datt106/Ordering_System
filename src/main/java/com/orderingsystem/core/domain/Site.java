package com.orderingsystem.core.domain;

import java.time.Instant;

public class Site {

    private String siteCode;

    private String siteName;

    private Integer shipDays;

    private Integer airDays;

    private String otherInfo;

    private boolean active = true;

    private ShippingStatus shippingStatus = ShippingStatus.CHUA_KHAI_BAO;

    private Instant shippingUpdatedAt;

    protected Site() {
    }

    public Site(String siteCode, String siteName, String otherInfo) {
        this.siteCode = siteCode;
        this.siteName = siteName;
        this.otherInfo = otherInfo;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public Integer getShipDays() {
        return shipDays;
    }

    public void setShipDays(Integer shipDays) {
        this.shipDays = shipDays;
    }

    public Integer getAirDays() {
        return airDays;
    }

    public void setAirDays(Integer airDays) {
        this.airDays = airDays;
    }

    public String getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(String otherInfo) {
        this.otherInfo = otherInfo;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ShippingStatus getShippingStatus() {
        return shippingStatus;
    }

    public void setShippingStatus(ShippingStatus shippingStatus) {
        this.shippingStatus = shippingStatus;
    }

    public Instant getShippingUpdatedAt() {
        return shippingUpdatedAt;
    }

    public void setShippingUpdatedAt(Instant shippingUpdatedAt) {
        this.shippingUpdatedAt = shippingUpdatedAt;
    }
}
