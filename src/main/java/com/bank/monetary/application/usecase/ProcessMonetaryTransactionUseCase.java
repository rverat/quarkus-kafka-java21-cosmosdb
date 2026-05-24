package com.bank.monetary.application.usecase;

import com.bank.monetary.application.dto.MonetaryTransactionCommand;
import com.bank.monetary.application.dto.MonetaryTransactionResult;
import com.bank.monetary.application.dto.TransactionPayload;
import com.bank.monetary.application.service.MetadataEnricher;
import com.bank.monetary.domain.exception.InvalidTransactionException;
import com.bank.monetary.domain.model.MonetaryTransaction;
import com.bank.monetary.domain.model.OutboxEvent;
import com.bank.monetary.domain.model.TransactionType;
import com.bank.monetary.domain.port.MonetaryTransactionRepository;
import com.bank.monetary.domain.vo.AccountId;
import com.bank.monetary.domain.vo.Money;
import com.bank.monetary.domain.vo.TransactionId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;

@ApplicationScoped
public class ProcessMonetaryTransactionUseCase {

    @Inject
    MonetaryTransactionRepository repository;

    @Inject
    MetadataEnricher enricher;

    @Inject
    ObjectMapper mapper;

    public MonetaryTransactionResult execute(MonetaryTransactionCommand cmd,
                                             Map<String, String> inboundHeaders) {

        if (cmd.idempotencyKey() == null || cmd.idempotencyKey().isBlank()) {
            throw new InvalidTransactionException("Header X-Idempotency-Key requerido");
        }

        Map<String, String> enriched = enricher.enrich(inboundHeaders);

        MonetaryTransaction tx = MonetaryTransaction.confirm(
                new TransactionId(cmd.idempotencyKey()),
                new AccountId(cmd.accountId()),
                new Money(cmd.amount(), cmd.currency()),
                TransactionType.valueOf(cmd.type()),
                enriched
        );

        String payload;
        try {
            payload = mapper.writeValueAsString(TransactionPayload.from(tx));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar payload outbox", e);
        }

        OutboxEvent outbox = new OutboxEvent(
                tx.id(),
                tx.accountId(),
                "MonetaryTransactionConfirmed",
                payload,
                enriched
        );

        repository.saveAtomic(tx, outbox);
        return MonetaryTransactionResult.from(tx);
    }
}
