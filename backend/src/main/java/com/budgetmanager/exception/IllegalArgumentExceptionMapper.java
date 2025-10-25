package com.budgetmanager.exception;

import com.budgetmanager.dto.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * Exception handler for IllegalArgumentException.
 * Typically indicates invalid input or business rule violations.
 */
@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    private static final Logger LOGGER = Logger.getLogger(IllegalArgumentExceptionMapper.class);

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        LOGGER.info("Invalid argument exception: " + exception.getMessage());

        String message = exception.getMessage() != null
                ? exception.getMessage()
                : "Paramètre invalide";

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(message))
                .build();
    }
}
