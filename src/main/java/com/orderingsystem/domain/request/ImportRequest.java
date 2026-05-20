package com.orderingsystem.domain.request;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "import_request")
public class ImportRequest {

    @Id
    @Column(name = "request_id", length = 32)
    private String requestId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, length = 64)
    private String createdBy;

    @Column(nullable = false, length = 64)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RequestStatus status = RequestStatus.CHO_XU_LY;

    @Column(name = "processed_by", length = 64)
    private String processedBy;

    @Column(name = "processed_at")
    private Instant processedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
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
