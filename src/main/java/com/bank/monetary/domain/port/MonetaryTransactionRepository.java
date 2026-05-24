package com.bank.monetary.domain.port;

import com.bank.monetary.domain.model.MonetaryTransaction;
import com.bank.monetary.domain.model.OutboxEvent;

public interface MonetaryTransactionRepository {
    void saveAtomic(MonetaryTransaction tx, OutboxEvent outbox);
}
