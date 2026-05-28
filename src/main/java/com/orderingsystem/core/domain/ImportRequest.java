package com.orderingsystem.core.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ImportRequest {

    private String requestId;

    private Instant createdAt;

    private String createdBy;

    private String department;

    private RequestStatus status = RequestStatus.CHO_XU_LY;

    private String processedBy;

    private Instant processedAt;

    private List<ImportRequestItem> items = new ArrayList<>();

    protected ImportRequest() {
    }

    public ImportRequest(String requestId, String createdBy, String department) {
        this.requestId = requestId;
        this.createdBy = createdBy;
        this.department = department;
        this.createdAt = Instant.now();
    }

    public String getRequestId() {
        return requestId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getDepartment() {
        return department;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public List<ImportRequestItem> getItems() {
        return items;
    }

    public void addItem(ImportRequestItem item) {
        items.add(item);
        item.setRequest(this);
    }
}
