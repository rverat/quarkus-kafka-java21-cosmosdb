package com.bank.monetary.domain.model;

import com.bank.monetary.domain.vo.AccountId;
import com.bank.monetary.domain.vo.TransactionId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public final class OutboxEvent {

    private final String id;
    private final TransactionId aggregateId;
    private final AccountId partitionKey;
    private final String eventType;
    private final String payloadJson;
    private final Map<String, String> headers;
    private final Instant createdAt;

    public OutboxEvent(TransactionId aggregateId,
                       AccountId partitionKey,
                       String eventType,
                       String payloadJson,
                       Map<String, String> headers) {
        Objects.requireNonNull(aggregateId);
        Objects.requireNonNull(partitionKey);
        Objects.requireNonNull(eventType);
        Objects.requireNonNull(payloadJson);
        Objects.requireNonNull(headers);
        this.id = aggregateId.value() + "-outbox";
        this.aggregateId = aggregateId;
        this.partitionKey = partitionKey;
        this.eventType = eventType;
        this.payloadJson = payloadJson;
        this.headers = Map.copyOf(headers);
        this.createdAt = Instant.now();
    }

    public String id()                       { return id; }
    public TransactionId aggregateId()       { return aggregateId; }
    public AccountId partitionKey()          { return partitionKey; }
    public String eventType()                { return eventType; }
    public String payloadJson()              { return payloadJson; }
    public Map<String, String> headers()     { return headers; }
    public Instant createdAt()               { return createdAt; }
}
