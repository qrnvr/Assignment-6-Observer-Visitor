package com.arnur.transport.observer;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record TransportEvent(
        EventType type,
        String sourceId,
        String routeId,
        Instant occurredAt,
        Map<String, String> attributes) {

    public TransportEvent {
        Objects.requireNonNull(type);
        Objects.requireNonNull(sourceId);
        Objects.requireNonNull(occurredAt);
        attributes = Map.copyOf(attributes == null ? Map.of() : attributes);
    }

    public String attribute(String key) {
        return attributes.get(key);
    }

    public int intAttribute(String key) {
        String value = attributes.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing event attribute: " + key);
        }
        return Integer.parseInt(value);
    }
}
