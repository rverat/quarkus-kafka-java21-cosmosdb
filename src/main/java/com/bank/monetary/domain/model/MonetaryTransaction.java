package com.bank.monetary.domain.model;

import com.bank.monetary.domain.vo.AccountId;
import com.bank.monetary.domain.vo.Money;
import com.bank.monetary.domain.vo.TransactionId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public final class MonetaryTransaction {

    private final TransactionId id;
    private final AccountId accountId;
    private final Money amount;
    private final TransactionType type;
    private final Instant occurredAt;
    private final TransactionStatus status;
    private final Map<String, String> metadata;

    private MonetaryTransaction(TransactionId id, AccountId accountId, Money amount,
                                TransactionType type, Instant occurredAt,
                                TransactionStatus status, Map<String, String> metadata) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.occurredAt = occurredAt;
        this.status = status;
        this.metadata = Map.copyOf(metadata);
    }

    public static MonetaryTransaction confirm(TransactionId id,
                                              AccountId accountId,
                                              Money amount,
                                              TransactionType type,
                                              Map<String, String> metadata) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(metadata, "metadata");
        return new MonetaryTransaction(
                id, accountId, amount, type,
                Instant.now(), TransactionStatus.CONFIRMED, metadata);
    }

    public TransactionId id()              { return id; }
    public AccountId accountId()           { return accountId; }
    public Money amount()                  { return amount; }
    public TransactionType type()          { return type; }
    public Instant occurredAt()            { return occurredAt; }
    public TransactionStatus status()      { return status; }
    public Map<String, String> metadata()  { return metadata; }
}
