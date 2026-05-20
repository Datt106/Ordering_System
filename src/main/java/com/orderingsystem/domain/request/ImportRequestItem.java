package com.orderingsystem.domain.request;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "import_request_item")
public class ImportRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private ImportRequest request;

    @Column(name = "merchandise_code", nullable = false, length = 64)
    private String merchandiseCode;

    @Column(name = "quantity_ordered", nullable = false)
    private int quantityOrdered;

    @Column(nullable = false, length = 32)
    private String unit;

    @Column(name = "desired_delivery_date", nullable = false)
    private LocalDate desiredDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false, length = 32)
    private ItemStatus itemStatus = ItemStatus.OK;

    protected ImportRequestItem() {
    }

    public ImportRequestItem(
            String merchandiseCode,
            int quantityOrdered,
            String unit,
            LocalDate desiredDeliveryDate
    ) {
        this.merchandiseCode = merchandiseCode;
        this.quantityOrdered = quantityOrdered;
        this.unit = unit;
        this.desiredDeliveryDate = desiredDeliveryDate;
    }

    public Long getId() {
        return id;
    }

    public ImportRequest getRequest() {
        return request;
    }

    void setRequest(ImportRequest request) {
        this.request = request;
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

    public LocalDate getDesiredDeliveryDate() {
        return desiredDeliveryDate;
    }

    public ItemStatus getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(ItemStatus itemStatus) {
        this.itemStatus = itemStatus;
    }
}
