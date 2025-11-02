package com.labsafer.workorder.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkOrderResponse(
        UUID id,
        UUID customerId,
        String title,
        String description,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
