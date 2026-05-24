package com.bank.monetary.infrastructure.rest;

import com.bank.monetary.application.dto.MonetaryTransactionCommand;
import com.bank.monetary.application.dto.MonetaryTransactionResult;
import com.bank.monetary.application.usecase.ProcessMonetaryTransactionUseCase;
import com.bank.monetary.infrastructure.rest.dto.MonetaryTransactionRequest;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

@Path("/api/v1/monetary-transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MonetaryTransactionResource {

    @Inject
    ProcessMonetaryTransactionUseCase useCase;

    @POST
    @RunOnVirtualThread
    public Response process(@Context HttpHeaders headers,
                            @Valid MonetaryTransactionRequest req) {

        Map<String, String> inbound = new HashMap<>();
        inbound.put("X-Correlation-ID", headers.getHeaderString("X-Correlation-ID"));
        inbound.put("X-Channel", headers.getHeaderString("X-Channel"));
        inbound.put("X-IP-Address", headers.getHeaderString("X-IP-Address"));

        String idempotencyKey = headers.getHeaderString("X-Idempotency-Key");

        MonetaryTransactionCommand command = new MonetaryTransactionCommand(
                idempotencyKey,
                req.accountId(),
                req.amount(),
                req.currency(),
                req.type()
        );

        MonetaryTransactionResult result = useCase.execute(command, inbound);

        return Response.status(Response.Status.CREATED)
                .entity(result)
                .header("X-Transaction-Id", result.transactionId())
                .build();
    }
}
