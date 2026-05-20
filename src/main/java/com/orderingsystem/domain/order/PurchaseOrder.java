package com.orderingsystem.domain.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "purchase_order")
public class PurchaseOrder {

    @Id
    @Column(name = "order_id", length = 64)
    private String orderId;

    @Column(name = "request_id", nullable = false, length = 32)
    private String requestId;

    @Column(name = "site_code", nullable = false, length = 32)
    private String siteCode;

    @Column(name = "merchandise_code", nullable = false, length = 64)
    private String merchandiseCode;

    @Column(name = "quantity_ordered", nullable = false)
    private int quantityOrdered;

    @Column(nullable = false, length = 32)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_means", nullable = false, length = 32)
    private DeliveryMeans deliveryMeans;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status = OrderStatus.CHO_GUI;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "actual_quantity")
    private Integer actualQuantity;

    @Column(name = "quantity_diff")
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
}
