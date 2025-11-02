package com.labsafer.workorder.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class WorkOrderRepository {

    @Inject EntityManager em;

    @Transactional
    public void persist(WorkOrderEntity entity) {
        if (em.find(WorkOrderEntity.class, entity.getId()) == null) {
            em.persist(entity);
        } else {
            em.merge(entity);
        }
    }

    public Optional<WorkOrderEntity> findByIdOptional(UUID id) {
        return Optional.ofNullable(em.find(WorkOrderEntity.class, id));
    }

    public List<WorkOrderEntity> findPaged(int page, int size) {
        return em.createQuery("from WorkOrderEntity o order by o.createdAt desc", WorkOrderEntity.class)
                .setFirstResult(Math.max(page, 0) * Math.max(size, 1))
                .setMaxResults(Math.max(size, 1))
                .getResultList();
    }

    @Transactional
    public void delete(WorkOrderEntity entity) {
        var managed = em.contains(entity) ? entity : em.merge(entity);
        em.remove(managed);
    }
}
