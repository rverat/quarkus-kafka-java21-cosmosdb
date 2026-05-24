package com.bank.monetary.domain.vo;

import java.util.Objects;

public record TransactionId(String value) {
    public TransactionId {
        Objects.requireNonNull(value, "TransactionId no puede ser null");
        if (value.isBlank() || value.length() > 64) {
            throw new IllegalArgumentException("TransactionId invalido: longitud 1..64");
        }
    }
}
