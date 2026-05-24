package com.bank.monetary.application.dto;

import java.math.BigDecimal;

public record MonetaryTransactionCommand(
        String idempotencyKey,
        String accountId,
        BigDecimal amount,
        String currency,
        String type
) {}
