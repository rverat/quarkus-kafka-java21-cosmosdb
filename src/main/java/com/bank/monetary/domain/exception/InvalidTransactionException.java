package com.bank.monetary.domain.exception;

public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) { super(message); }
}
