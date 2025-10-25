package com.budgetmanager.exception;

import com.budgetmanager.dto.ErrorResponse;
import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * Exception handler for OptimisticLockException.
 * Occurs when two requests try to update the same entity simultaneously.
 */
@Provider
public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException> {

    private static final Logger LOGGER = Logger.getLogger(OptimisticLockExceptionMapper.class);

    @Override
    public Response toResponse(OptimisticLockException exception) {
        LOGGER.warn("Optimistic lock exception occurred - concurrent modification detected", exception);

        String message = "Les données ont été modifiées par un autre utilisateur. "
                + "Veuillez rafraîchir et réessayer.";

        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(message))
                .build();
    }
}
