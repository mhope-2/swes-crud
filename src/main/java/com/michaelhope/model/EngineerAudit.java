package com.michaelhope.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "engineer_audit",
    uniqueConstraints = @UniqueConstraint(name = "uk_engineer_audit_event_id", columnNames = "event_id")
)
@Getter
@Setter
@NoArgsConstructor
public class EngineerAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false)
    private Integer aggregateId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "aggregate_version", nullable = false)
    private Integer aggregateVersion;

    @Column(name = "schema_version", nullable = false)
    private Integer schemaVersion;

    @Lob
    @Column(nullable = false)
    private String payload;

    public EngineerAudit(UUID eventId, String eventType, Integer aggregateId, Instant occurredAt,
                         Integer aggregateVersion, Integer schemaVersion, String payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.occurredAt = occurredAt;
        this.aggregateVersion = aggregateVersion;
        this.schemaVersion = schemaVersion;
        this.payload = payload;
    }
}
