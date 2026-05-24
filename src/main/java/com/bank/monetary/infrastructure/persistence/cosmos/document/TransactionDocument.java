package com.bank.monetary.infrastructure.persistence.cosmos.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDocument {

    @JsonProperty("id")           public String id;
    @JsonProperty("accountId")    public String accountId;
    @JsonProperty("docType")      public String docType = "transaction";
    @JsonProperty("amount")       public BigDecimal amount;
    @JsonProperty("currency")     public String currency;
    @JsonProperty("type")         public String type;
    @JsonProperty("status")       public String status;
    @JsonProperty("occurredAt")   public String occurredAt;
    @JsonProperty("metadata")     public Map<String, String> metadata;

    public TransactionDocument() {}
}
