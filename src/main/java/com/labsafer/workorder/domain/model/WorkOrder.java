package com.labsafer.workorder.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class WorkOrder {
    private UUID id;
    private UUID customerId;
    private String title;
    private String description;
    private WorkOrderStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public WorkOrder(UUID id, UUID customerId, String title, String description,
                     WorkOrderStatus status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public WorkOrderStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
