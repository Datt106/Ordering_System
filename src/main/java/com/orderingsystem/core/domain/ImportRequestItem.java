package com.orderingsystem.core.domain;

import java.time.LocalDate;

public class ImportRequestItem {

    private Long id;

    private ImportRequest request;

    private String merchandiseCode;

    private int quantityOrdered;

    private String unit;

    private LocalDate desiredDeliveryDate;

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
