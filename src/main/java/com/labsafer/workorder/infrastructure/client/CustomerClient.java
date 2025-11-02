package com.labsafer.workorder.infrastructure.client;

import com.labsafer.workorder.infrastructure.client.dto.CustomerDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v1/customers")
@RegisterRestClient(configKey = "com.labsafer.workorder.infrastructure.client.CustomerClient")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CustomerClient {

    @GET
    @Path("/{id}")
    CustomerDto getById(@PathParam("id") String id);
}
