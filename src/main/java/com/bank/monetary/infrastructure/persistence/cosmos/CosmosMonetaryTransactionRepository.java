package com.bank.monetary.infrastructure.persistence.cosmos;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.PartitionKey;
import com.bank.monetary.domain.exception.DuplicateTransactionException;
import com.bank.monetary.domain.exception.PersistenceException;
import com.bank.monetary.domain.model.MonetaryTransaction;
import com.bank.monetary.domain.model.OutboxEvent;
import com.bank.monetary.domain.port.MonetaryTransactionRepository;
import com.bank.monetary.infrastructure.persistence.cosmos.document.OutboxDocument;
import com.bank.monetary.infrastructure.persistence.cosmos.document.TransactionDocument;
import com.bank.monetary.infrastructure.persistence.cosmos.mapper.DocumentMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CosmosMonetaryTransactionRepository implements MonetaryTransactionRepository {

    private static final Logger LOG = Logger.getLogger(CosmosMonetaryTransactionRepository.class);
    private static final int HTTP_CONFLICT = 409;

    @Inject
    CosmosClient client;

    @ConfigProperty(name = "cosmos.database")
    String database;

    @ConfigProperty(name = "cosmos.container.transactions")
    String containerName;

    private CosmosContainer container;

    @PostConstruct
    void init() {
        this.container = client.getDatabase(database).getContainer(containerName);
    }

    @Override
    public void saveAtomic(MonetaryTransaction tx, OutboxEvent outbox) {

        PartitionKey pk = new PartitionKey(tx.accountId().value());

        TransactionDocument txDoc = DocumentMapper.toTransactionDoc(tx);
        OutboxDocument outboxDoc = DocumentMapper.toOutboxDoc(outbox);

        CosmosBatch batch = CosmosBatch.createCosmosBatch(pk);
        batch.createItemOperation(txDoc);
        batch.createItemOperation(outboxDoc);

        try {
            CosmosBatchResponse response =
                    container.executeCosmosBatch(batch, new CosmosBatchRequestOptions());

            if (!response.isSuccessStatusCode()) {
                int statusCode = response.getStatusCode();
                if (statusCode == HTTP_CONFLICT) {
                    throw new DuplicateTransactionException(tx.id().value());
                }
                throw new PersistenceException(
                        "CosmosBatch fallo status=" + statusCode +
                        " sub=" + response.getSubStatusCode() +
                        " err=" + response.getErrorMessage());
            }

            LOG.debugf("Batch OK accountId=%s txId=%s ru=%.2f",
                    tx.accountId().value(),
                    tx.id().value(),
                    response.getRequestCharge());

        } catch (CosmosException e) {
            if (e.getStatusCode() == HTTP_CONFLICT) {
                throw new DuplicateTransactionException(tx.id().value());
            }
            throw new PersistenceException("CosmosException en saveAtomic", e);
        }
    }
}
