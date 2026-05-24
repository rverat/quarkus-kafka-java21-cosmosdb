package com.bank.monetary.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class MetadataEnricher {

    @ConfigProperty(name = "app.node-id")
    String nodeId;

    @ConfigProperty(name = "app.version")
    String appVersion;

    public Map<String, String> enrich(Map<String, String> inbound) {
        Map<String, String> out = new HashMap<>();
        if (inbound != null) {
            inbound.forEach((k, v) -> {
                if (k != null && v != null && !v.isBlank()) {
                    out.put(k, v);
                }
            });
        }
        out.put("X-Processed-At", Instant.now().toString());
        out.put("X-Node-ID", nodeId);
        out.put("X-App-Version", appVersion);
        return Map.copyOf(out);
    }
}
