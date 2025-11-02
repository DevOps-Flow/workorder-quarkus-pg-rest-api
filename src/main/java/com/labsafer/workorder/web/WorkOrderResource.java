package com.labsafer.workorder.web;

import com.labsafer.workorder.application.ports.WorkOrderService;
import com.labsafer.workorder.domain.model.WorkOrderStatus;
import com.labsafer.workorder.web.dto.WorkOrderRequest;
import com.labsafer.workorder.web.dto.WorkOrderResponse;
import com.labsafer.workorder.web.exception.NotFoundException;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/v1/work-orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WorkOrderResource {

    @Inject WorkOrderService service;

    @POST
    public Response create(@Valid WorkOrderRequest req) {
        var wo = service.create(req.getCustomerId(), req.getTitle(), req.getDescription());
        var resp = new WorkOrderResponse(wo.getId(), wo.getCustomerId(), wo.getTitle(),
                wo.getDescription(), wo.getStatus().name(), wo.getCreatedAt(), wo.getUpdatedAt());
        return Response.status(Response.Status.CREATED).entity(resp).build();
    }

    @GET
    @Path("/{id}")
    public WorkOrderResponse get(@PathParam("id") UUID id) {
        var wo = service.findById(id).orElseThrow(() -> new NotFoundException("Ordem não encontrada"));
        return new WorkOrderResponse(wo.getId(), wo.getCustomerId(), wo.getTitle(),
                wo.getDescription(), wo.getStatus().name(), wo.getCreatedAt(), wo.getUpdatedAt());
    }

    @GET
    public Response list(@QueryParam("page") @DefaultValue("0") int page,
                         @QueryParam("size") @DefaultValue("20") int size) {
        var list = service.findAll(page, size).stream().map(wo ->
                new WorkOrderResponse(wo.getId(), wo.getCustomerId(), wo.getTitle(), wo.getDescription(),
                        wo.getStatus().name(), wo.getCreatedAt(), wo.getUpdatedAt())).toList();
        return Response.ok(list).build();
    }

    @PUT
    @Path("/{id}")
    public WorkOrderResponse update(@PathParam("id") UUID id, WorkOrderRequest req,
                                    @QueryParam("status") String status) {
        var newStatus = status != null ? WorkOrderStatus.valueOf(status) : null;
        var wo = service.update(id, req.getTitle(), req.getDescription(), newStatus);
        return new WorkOrderResponse(wo.getId(), wo.getCustomerId(), wo.getTitle(),
                wo.getDescription(), wo.getStatus().name(), wo.getCreatedAt(), wo.getUpdatedAt());
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
