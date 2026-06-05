package com.orderingsystem.core.domain;

import java.time.Instant;

public class PurchaseOrder {

    private String orderId;

    private String requestId;

    private String siteCode;

    private String siteName; 

    private String merchandiseCode;
    
    private String merchandiseName;

    private int quantityOrdered;

    private String unit;

    private DeliveryMeans deliveryMeans;

    private OrderStatus status = OrderStatus.CHO_GUI;

    private Instant sentAt;

    private Instant confirmedAt;

    private Instant reconciledAt;

    private Integer actualQuantity;

    private Integer quantityDiff;

    protected PurchaseOrder() {
    }

    public PurchaseOrder(
            String orderId,
            String requestId,
            String siteCode,
            String merchandiseCode,
            int quantityOrdered,
            String unit,
            DeliveryMeans deliveryMeans
    ) {
        this.orderId = orderId;
        this.requestId = requestId;
        this.siteCode = siteCode;
        this.merchandiseCode = merchandiseCode;
        this.quantityOrdered = quantityOrdered;
        this.unit = unit;
        this.deliveryMeans = deliveryMeans;
    }

    public String getOrderId() {
        return orderId;
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

    public int getQuantityOrdered() {
        return quantityOrdered;
    }

    public String getUnit() {
        return unit;
    }

    public DeliveryMeans getDeliveryMeans() {
        return deliveryMeans;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Integer getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(Integer actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public Integer getQuantityDiff() {
        return quantityDiff;
    }

    public void setQuantityDiff(Integer quantityDiff) {
        this.quantityDiff = quantityDiff;
    }

    public String getSiteName() { 
        return siteName; 
    }
    
    public void setSiteName(String siteName) {
        this.siteName = siteName; 
    }

    public String getMerchandiseName() { 
        return merchandiseName; 
    }

    public void setMerchandiseName(String merchandiseName) {
        this.merchandiseName = merchandiseName; 
    }

    public Instant getReconciledAt() { 
        return reconciledAt; 
    }

    public void setReconciledAt(Instant reconciledAt) { 
        this.reconciledAt = reconciledAt; 
    }
}
