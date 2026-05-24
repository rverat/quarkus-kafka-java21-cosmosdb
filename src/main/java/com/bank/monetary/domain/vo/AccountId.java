package com.bank.monetary.domain.vo;

import java.util.Objects;

public record AccountId(String value) {
    public AccountId {
        Objects.requireNonNull(value, "AccountId no puede ser null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("AccountId vacio");
        }
    }
}
