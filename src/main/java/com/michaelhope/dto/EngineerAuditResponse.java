package com.michaelhope.dto;

import com.michaelhope.model.EngineerAudit;

import java.time.Instant;
import java.util.UUID;

public record EngineerAuditResponse(
    UUID eventId,
    String eventType,
    Integer aggregateId,
    Instant occurredAt,
    Integer aggregateVersion,
    Integer schemaVersion,
    String payload
) {

    public static EngineerAuditResponse from(EngineerAudit audit) {
        return new EngineerAuditResponse(
            audit.getEventId(),
            audit.getEventType(),
            audit.getAggregateId(),
            audit.getOccurredAt(),
            audit.getAggregateVersion(),
            audit.getSchemaVersion(),
            audit.getPayload()
        );
    }
}
