package com.orderingsystem.core.domain;

import java.time.Instant;

public class InventoryQuery {

    private String queryId;

    private String requestId;

    private String siteCode;

    private String merchandiseCode;

    private int inStockQuantity;

    private String unit;

    private Instant respondedAt;

    protected InventoryQuery() {
    }

    public InventoryQuery(
            String queryId,
            String requestId,
            String siteCode,
            String merchandiseCode,
            int inStockQuantity,
            String unit,
            Instant respondedAt
    ) {
        this.queryId = queryId;
        this.requestId = requestId;
        this.siteCode = siteCode;
        this.merchandiseCode = merchandiseCode;
        this.inStockQuantity = inStockQuantity;
        this.unit = unit;
        this.respondedAt = respondedAt;
    }

    public String getQueryId() {
        return queryId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public String getMerchandiseCode() {
        return merchandiseCode;
    }

    public int getInStockQuantity() {
        return inStockQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }

    public boolean isPending() {
        return respondedAt == null;
    }

    public void setInStockQuantity(int inStockQuantity) {
        this.inStockQuantity = inStockQuantity;
    }

    public void setRespondedAt(Instant respondedAt) {
        this.respondedAt = respondedAt;
    }
}
