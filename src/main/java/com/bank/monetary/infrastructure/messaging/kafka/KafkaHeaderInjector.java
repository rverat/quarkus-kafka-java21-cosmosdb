package com.bank.monetary.infrastructure.messaging.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

public final class KafkaHeaderInjector {

    private KafkaHeaderInjector() {}

    public static void inject(ProducerRecord<String, String> record, JsonNode headersNode) {
        if (headersNode == null || !headersNode.isObject()) return;
        Iterator<Map.Entry<String, JsonNode>> it = headersNode.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> e = it.next();
            JsonNode value = e.getValue();
            if (value == null || value.isNull()) continue;
            String stringValue = value.asText();
            if (stringValue == null) continue;
            record.headers().add(e.getKey(), stringValue.getBytes(StandardCharsets.UTF_8));
        }
    }
}
