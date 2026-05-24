package com.bank.monetary.domain.vo;

import java.math.BigDecimal;
import java.util.Objects;

public record Money(BigDecimal amount, String currency) {
    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount debe ser > 0");
        }
        if (currency.length() != 3) {
            throw new IllegalArgumentException("currency debe ser ISO-4217 (3 letras)");
        }
    }
}
