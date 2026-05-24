package com.bank.monetary.infrastructure.messaging.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Properties;

@ApplicationScoped
public class KafkaProducerProducer {

    @ConfigProperty(name = "kafka.bootstrap.servers")  String bootstrap;
    @ConfigProperty(name = "kafka.client.id")          String clientId;
    @ConfigProperty(name = "kafka.acks")               String acks;
    @ConfigProperty(name = "kafka.enable.idempotence") boolean idempotence;
    @ConfigProperty(name = "kafka.max.in.flight.requests.per.connection") int maxInFlight;
    @ConfigProperty(name = "kafka.retries")            int retries;
    @ConfigProperty(name = "kafka.delivery.timeout.ms") int deliveryTimeout;
    @ConfigProperty(name = "kafka.request.timeout.ms") int requestTimeout;
    @ConfigProperty(name = "kafka.linger.ms")          int linger;
    @ConfigProperty(name = "kafka.compression.type")   String compression;

    @Produces
    @Singleton
    public KafkaProducer<String, String> producer() {
        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        p.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        p.put(ProducerConfig.ACKS_CONFIG, acks);
        p.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, idempotence);
        p.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlight);
        p.put(ProducerConfig.RETRIES_CONFIG, retries);
        p.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeout);
        p.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);
        p.put(ProducerConfig.LINGER_MS_CONFIG, linger);
        p.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compression);
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(p);
    }

    public void close(@Disposes KafkaProducer<String, String> producer) {
        producer.close();
    }
}
