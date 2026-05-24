package com.bank.monetary.infrastructure.persistence.cosmos.mapper;

import com.bank.monetary.domain.model.MonetaryTransaction;
import com.bank.monetary.domain.model.OutboxEvent;
import com.bank.monetary.infrastructure.persistence.cosmos.document.OutboxDocument;
import com.bank.monetary.infrastructure.persistence.cosmos.document.TransactionDocument;

public final class DocumentMapper {

    private DocumentMapper() {}

    public static TransactionDocument toTransactionDoc(MonetaryTransaction tx) {
        TransactionDocument d = new TransactionDocument();
        d.id = tx.id().value();
        d.accountId = tx.accountId().value();
        d.amount = tx.amount().amount();
        d.currency = tx.amount().currency();
        d.type = tx.type().name();
        d.status = tx.status().name();
        d.occurredAt = tx.occurredAt().toString();
        d.metadata = tx.metadata();
        return d;
    }

    public static OutboxDocument toOutboxDoc(OutboxEvent ev) {
        OutboxDocument d = new OutboxDocument();
        d.id = ev.id();
        d.accountId = ev.partitionKey().value();
        d.idempotencyKey = ev.aggregateId().value();
        d.eventType = ev.eventType();
        d.payload = ev.payloadJson();
        d.headers = ev.headers();
        d.createdAt = ev.createdAt().toString();
        d.ttl = 86400;
        return d;
    }
}
