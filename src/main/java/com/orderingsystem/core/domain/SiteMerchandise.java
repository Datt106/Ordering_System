package com.orderingsystem.core.domain;

import java.time.Instant;

public class SiteMerchandise {

    private Long id;

    private Site site;

    private String merchandiseCode;

    private Instant updatedAt = Instant.now();

    protected SiteMerchandise() {
    }

    public SiteMerchandise(Site site, String merchandiseCode) {
        this.site = site;
        this.merchandiseCode = merchandiseCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public String getMerchandiseCode() {
        return merchandiseCode;
    }

    public void setMerchandiseCode(String merchandiseCode) {
        this.merchandiseCode = merchandiseCode;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void touchUpdatedAt() {
        this.updatedAt = Instant.now();
    }
}
