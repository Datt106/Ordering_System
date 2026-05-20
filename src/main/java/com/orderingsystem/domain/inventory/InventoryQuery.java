package com.orderingsystem.domain.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "inventory_query")
public class InventoryQuery {

    @Id
    @Column(name = "query_id", length = 64)
    private String queryId;

    @Column(name = "request_id", nullable = false, length = 32)
    private String requestId;

    @Column(name = "site_code", nullable = false, length = 32)
    private String siteCode;

    @Column(name = "merchandise_code", nullable = false, length = 64)
    private String merchandiseCode;

    @Column(name = "in_stock_quantity", nullable = false)
    private int inStockQuantity;

    @Column(nullable = false, length = 32)
    private String unit;

    @Column(name = "responded_at")
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
}
