package com.bank.monetary.domain.port;

import com.bank.monetary.domain.model.OutboxEvent;

public interface EventDispatcher {
    void dispatch(OutboxEvent event);
}
