package com.budgetmanager.exception;

import com.budgetmanager.dto.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Global exception handler for unexpected exceptions.
 * Catches all exceptions not handled by more specific mappers.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class);

    @ConfigProperty(name = "quarkus.profile")
    String profile;

    @Override
    public Response toResponse(Exception exception) {
        // Si c'est déjà une WebApplicationException, laisser le mapper spécifique la gérer
        if (exception instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) exception;
            return wae.getResponse();
        }

        // Log l'exception avec détails
        LOGGER.error("Unexpected exception occurred", exception);

        // En production, ne jamais exposer les détails d'erreur internes
        String message;
        if ("dev".equals(profile) || "test".equals(profile)) {
            // En dev/test, inclure le message pour faciliter le debug
            message = exception.getMessage() != null ? exception.getMessage() : "Une erreur interne est survenue";
        } else {
            // En production, message générique uniquement
            message = "Une erreur interne est survenue. Veuillez contacter le support.";
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(message))
                .build();
    }
}
