package com.bank.monetary.application.dto;

import com.bank.monetary.domain.model.MonetaryTransaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionPayload(
        String transactionId,
        String accountId,
        BigDecimal amount,
        String currency,
        String type,
        String status,
        Instant occurredAt
) {
    public static TransactionPayload from(MonetaryTransaction tx) {
        return new TransactionPayload(
                tx.id().value(),
                tx.accountId().value(),
                tx.amount().amount(),
                tx.amount().currency(),
                tx.type().name(),
                tx.status().name(),
                tx.occurredAt()
        );
    }
}
