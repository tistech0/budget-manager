package com.budgetmanager.exception;

import com.budgetmanager.dto.ErrorResponse;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Global exception handler for NotFoundException.
 * Standardizes 404 error responses across the application.
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        String message = exception.getMessage();

        // Si le message est vide ou générique, utiliser un message par défaut
        if (message == null || message.contains("RESTEASY")) {
            message = "Ressource non trouvée";
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(message))
                .build();
    }
}
