package com.labsafer.workorder.infrastructure.mapper;

import com.labsafer.workorder.domain.model.WorkOrder;
import com.labsafer.workorder.domain.model.WorkOrderStatus;
import com.labsafer.workorder.infrastructure.persistence.WorkOrderEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WorkOrderMapper {

    public WorkOrder toDomain(WorkOrderEntity e) {
        return new WorkOrder(
                e.getId(),
                e.getCustomerId(),
                e.getTitle(),
                e.getDescription(),
                WorkOrderStatus.valueOf(e.getStatus()),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
