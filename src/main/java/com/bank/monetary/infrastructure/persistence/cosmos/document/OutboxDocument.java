package com.bank.monetary.infrastructure.persistence.cosmos.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutboxDocument {

    @JsonProperty("id")             public String id;
    @JsonProperty("accountId")      public String accountId;
    @JsonProperty("docType")        public String docType = "outbox";
    @JsonProperty("idempotencyKey") public String idempotencyKey;
    @JsonProperty("eventType")      public String eventType;
    @JsonProperty("payload")        public String payload;
    @JsonProperty("headers")        public Map<String, String> headers;
    @JsonProperty("createdAt")      public String createdAt;
    @JsonProperty("ttl")            public Integer ttl;

    public OutboxDocument() {}
}
