package com.orderingsystem.domain.site;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "site")
public class Site {

    @Id
    @Column(name = "site_code", length = 32)
    private String siteCode;

    @Column(name = "site_name", nullable = false, length = 255)
    private String siteName;

    @Column(name = "ship_days")
    private Integer shipDays;

    @Column(name = "air_days")
    private Integer airDays;

    @Column(name = "other_info", length = 2000)
    private String otherInfo;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_status", nullable = false, length = 32)
    private ShippingStatus shippingStatus = ShippingStatus.CHUA_KHAI_BAO;

    @Column(name = "shipping_updated_at")
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
