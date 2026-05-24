package com.bank.monetary.domain.exception;

public class DuplicateTransactionException extends RuntimeException {
    private final String transactionId;

    public DuplicateTransactionException(String transactionId) {
        super("Transaccion duplicada: " + transactionId);
        this.transactionId = transactionId;
    }

    public String transactionId() { return transactionId; }
}
