package com.bank.monetary.infrastructure.rest.error;

import com.bank.monetary.domain.exception.DuplicateTransactionException;
import com.bank.monetary.domain.exception.InvalidTransactionException;
import com.bank.monetary.domain.exception.PersistenceException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable ex) {
        if (ex instanceof DuplicateTransactionException dup) {
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of(
                            "error", "DUPLICATE_TRANSACTION",
                            "transactionId", dup.transactionId(),
                            "message", dup.getMessage()))
                    .build();
        }
        if (ex instanceof InvalidTransactionException || ex instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", "INVALID_REQUEST", "message", ex.getMessage()))
                    .build();
        }
        if (ex instanceof PersistenceException) {
            LOG.error("Persistence error", ex);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", "PERSISTENCE_ERROR", "message", ex.getMessage()))
                    .build();
        }
        LOG.error("Unhandled error", ex);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", "INTERNAL_ERROR", "message", "Unexpected error"))
                .build();
    }
}
