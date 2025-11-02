package com.labsafer.workorder.web.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.validation.ConstraintViolationException;

import java.time.OffsetDateTime;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<Exception> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception ex) {
        int status = 500;
        String error = "Internal Server Error";

        if (ex instanceof NotFoundException) {
            status = 404;
            error = "Not Found";
        } else if (ex instanceof BadRequestException) {
            status = 400;
            error = "Bad Request";
        } else if (ex instanceof ConstraintViolationException) {
            status = 400;
            error = "Validation Error";
        }

        var path = uriInfo != null ? uriInfo.getPath() : "N/A";
        var body = new ApiError(status, error, ex.getMessage(), path, OffsetDateTime.now());
        return Response.status(status).entity(body).build();
    }
}
