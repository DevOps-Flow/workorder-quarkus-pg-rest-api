package com.labsafer.workorder.application.services;

import com.labsafer.workorder.application.ports.WorkOrderService;
import com.labsafer.workorder.domain.model.WorkOrder;
import com.labsafer.workorder.domain.model.WorkOrderStatus;
import com.labsafer.workorder.infrastructure.client.CustomerClient;
import com.labsafer.workorder.infrastructure.persistence.WorkOrderEntity;
import com.labsafer.workorder.infrastructure.persistence.WorkOrderRepository;
import com.labsafer.workorder.infrastructure.mapper.WorkOrderMapper;
import com.labsafer.workorder.web.exception.BadRequestException;
import com.labsafer.workorder.web.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class WorkOrderServiceImpl implements WorkOrderService {

    @Inject WorkOrderRepository repository;
    @Inject WorkOrderMapper mapper;
    @Inject @RestClient CustomerClient customerClient;

    @Override
    public WorkOrder create(UUID customerId, String title, String description) {
        // Verifica cliente no sistema de cadastro
        var customer = customerClient.getById(customerId.toString());
        if (customer == null || customer.id() == null) {
            throw new BadRequestException("Cliente inexistente: " + customerId);
        }

        var now = OffsetDateTime.now();
        var entity = new WorkOrderEntity();
        entity.setId(UUID.randomUUID());
        entity.setCustomerId(customerId);
        entity.setTitle(title);
        entity.setDescription(description);
        entity.setStatus("OPEN");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(null);

        repository.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<WorkOrder> findById(UUID id) {
        return repository.findByIdOptional(id).map(mapper::toDomain);
    }

    @Override
    public List<WorkOrder> findAll(int page, int size) {
        return repository.findPaged(page, size).stream().map(mapper::toDomain).toList();
    }

    @Override
    public WorkOrder update(UUID id, String title, String description, WorkOrderStatus status) {
        var entity = repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada: " + id));

        if (title != null) entity.setTitle(title);
        if (description != null) entity.setDescription(description);
        if (status != null) entity.setStatus(status.name());
        entity.setUpdatedAt(OffsetDateTime.now());

        repository.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public void delete(UUID id) {
        var entity = repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada: " + id));
        repository.delete(entity);
    }
}
