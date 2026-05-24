package com.bank.monetary.infrastructure.messaging.kafka;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Dispatcher Outbox -> Kafka basado en Change Feed Processor de Cosmos.
 *
 * - El handler corre dentro de un hilo virtual (Loom): los future.get() bloqueantes son baratos.
 * - acks=all + future.get() asegura ACK del broker ANTES de retornar -> checkpoint solo tras ACK.
 * - Si el envio principal agota reintentos, se publica al topico DLQ con headers X-Death-*.
 * - Si la DLQ tambien falla, propagamos la excepcion -> el SDK NO hace checkpoint -> reintento del lote.
 */
@ApplicationScoped
public class OutboxChangeFeedDispatcher {

    private static final Logger LOG = Logger.getLogger(OutboxChangeFeedDispatcher.class);
    private static final long ACK_TIMEOUT_SECONDS = 30L;

    @Inject CosmosAsyncClient asyncClient;
    @Inject KafkaProducer<String, String> kafkaProducer;
    @Inject ObjectMapper mapper;

    @ConfigProperty(name = "cosmos.database")              String database;
    @ConfigProperty(name = "cosmos.container.transactions") String feedContainerName;
    @ConfigProperty(name = "cosmos.container.leases")      String leaseContainerName;
    @ConfigProperty(name = "changefeed.host-name")         String hostName;
    @ConfigProperty(name = "changefeed.lease-prefix")      String leasePrefix;
    @ConfigProperty(name = "changefeed.max-items-per-batch") int maxItemsPerBatch;
    @ConfigProperty(name = "kafka.topic.success")          String topicSuccess;
    @ConfigProperty(name = "kafka.topic.dlq")              String topicDlq;

    private ChangeFeedProcessor processor;

    private final ExecutorService loomExecutor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("cf-dispatch-", 0).factory());

    void onStart(@Observes StartupEvent event) {
        CosmosAsyncDatabase db = asyncClient.getDatabase(database);
        CosmosAsyncContainer feed = db.getContainer(feedContainerName);
        CosmosAsyncContainer lease = db.getContainer(leaseContainerName);

        ChangeFeedProcessorOptions opts = new ChangeFeedProcessorOptions();
        opts.setMaxItemCount(maxItemsPerBatch);
        opts.setLeasePrefix(leasePrefix);
        opts.setStartFromBeginning(false);

        processor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .feedContainer(feed)
                .leaseContainer(lease)
                .options(opts)
                .handleChanges(this::handleChangesBlocking)
                .buildChangeFeedProcessor();

        processor.start().block();
        LOG.infof("ChangeFeedProcessor iniciado host=%s feed=%s leases=%s",
                hostName, feedContainerName, leaseContainerName);
    }

    void onStop(@Observes ShutdownEvent event) {
        if (processor != null) {
            try { processor.stop().block(); } catch (Exception ignored) {}
        }
        loomExecutor.shutdown();
    }

    private void handleChangesBlocking(List<JsonNode> docs) {
        try {
            loomExecutor.submit(() -> {
                for (JsonNode doc : docs) {
                    if (!"outbox".equals(doc.path("docType").asText())) {
                        continue;
                    }
                    publishWithDurability(doc);
                }
            }).get();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Dispatch interrumpido", ie);
        } catch (ExecutionException ee) {
            throw new RuntimeException("Dispatch fatal - sin checkpoint", ee.getCause());
        }
    }

    private void publishWithDurability(JsonNode outboxDoc) {
        String key = outboxDoc.path("idempotencyKey").asText();
        String payload = outboxDoc.path("payload").asText();
        JsonNode hdrs = outboxDoc.path("headers");

        ProducerRecord<String, String> record =
                new ProducerRecord<>(topicSuccess, null, null, key, payload);
        KafkaHeaderInjector.inject(record, hdrs);

        try {
            RecordMetadata md = kafkaProducer.send(record).get(ACK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            LOG.debugf("Kafka OK topic=%s partition=%d offset=%d key=%s",
                    md.topic(), md.partition(), md.offset(), key);
        } catch (TimeoutException | ExecutionException e) {
            Throwable cause = e instanceof ExecutionException && e.getCause() != null
                    ? e.getCause() : e;
            LOG.warnf(cause, "Fallo publicacion principal key=%s -> DLQ", key);
            sendToDlq(key, payload, hdrs, cause);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrumpido esperando ACK", ie);
        }
    }

    private void sendToDlq(String key, String payload, JsonNode originalHeaders, Throwable cause) {
        ProducerRecord<String, String> dlq =
                new ProducerRecord<>(topicDlq, null, null, key, payload);
        KafkaHeaderInjector.inject(dlq, originalHeaders);

        String reason = cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
        dlq.headers().add("X-Death-Reason", reason.getBytes(StandardCharsets.UTF_8));
        dlq.headers().add("X-Death-Class", cause.getClass().getName().getBytes(StandardCharsets.UTF_8));
        dlq.headers().add("X-Death-Topic", topicSuccess.getBytes(StandardCharsets.UTF_8));

        try {
            RecordMetadata md = kafkaProducer.send(dlq).get(ACK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            LOG.infof("DLQ OK topic=%s partition=%d offset=%d key=%s",
                    md.topic(), md.partition(), md.offset(), key);
        } catch (Exception fatal) {
            throw new RuntimeException("Fallo en DLQ - sin checkpoint key=" + key, fatal);
        }
    }
}
