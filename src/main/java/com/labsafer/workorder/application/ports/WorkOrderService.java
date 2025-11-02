package com.labsafer.workorder.application.ports;

import com.labsafer.workorder.domain.model.WorkOrder;
import com.labsafer.workorder.domain.model.WorkOrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkOrderService {
    WorkOrder create(UUID customerId, String title, String description);
    Optional<WorkOrder> findById(UUID id);
    List<WorkOrder> findAll(int page, int size);
    WorkOrder update(UUID id, String title, String description, WorkOrderStatus status);
    void delete(UUID id);
}
