package com.michaelhope.event;

import java.time.Instant;
import java.util.UUID;

public record SoftwareEngineerEvent(
    UUID eventId,
    String eventType,
    Integer aggregateId,
    Instant occurredAt,
    Integer aggregateVersion,
    String payload,
    Integer schemaVersion
) {

    public SoftwareEngineerEvent(
            UUID eventId,
            String eventType,
            Integer aggregateId,
            Instant occurredAt,
            Integer aggregateVersion,
            String payload) {
        this(eventId, eventType, aggregateId, occurredAt, aggregateVersion, payload, 1);
    }
}
