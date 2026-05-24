package com.bank.monetary.infrastructure.persistence.cosmos;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@ApplicationScoped
public class CosmosClientProducer {

    @ConfigProperty(name = "cosmos.endpoint")
    String endpoint;

    @ConfigProperty(name = "cosmos.key")
    String key;

    @ConfigProperty(name = "cosmos.preferred-regions", defaultValue = "East US")
    List<String> regions;

    @Produces
    @Singleton
    public CosmosClient cosmosClient() {
        return new CosmosClientBuilder()
                .endpoint(endpoint)
                .key(key)
                .preferredRegions(regions)
                .consistencyLevel(ConsistencyLevel.SESSION)
                .directMode()
                .contentResponseOnWriteEnabled(false)
                .buildClient();
    }

    @Produces
    @Singleton
    public CosmosAsyncClient cosmosAsyncClient() {
        return new CosmosClientBuilder()
                .endpoint(endpoint)
                .key(key)
                .preferredRegions(regions)
                .consistencyLevel(ConsistencyLevel.SESSION)
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();
    }

    public void closeSync(@Disposes CosmosClient client) {
        client.close();
    }

    public void closeAsync(@Disposes CosmosAsyncClient client) {
        client.close();
    }
}
