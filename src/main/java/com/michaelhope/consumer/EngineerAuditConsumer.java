package com.michaelhope.consumer;

import com.michaelhope.event.KafkaTopics;
import com.michaelhope.event.SoftwareEngineerEvent;
import com.michaelhope.model.EngineerAudit;
import com.michaelhope.repository.EngineerAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class EngineerAuditConsumer {

    private final EngineerAuditRepository repository;

    @KafkaListener(topics = KafkaTopics.SOFTWARE_ENGINEER_EVENTS_V1)
    public void consume(ConsumerRecord<String, SoftwareEngineerEvent> record) {
        SoftwareEngineerEvent event = record.value();
        long startedAt = System.nanoTime();
        log.debug("event.received eventId={} eventType={} aggregateId={} aggregateVersion={} schemaVersion={} "
                + "topic={} partition={} offset={} eventAgeMs={}",
            event.eventId(), event.eventType(), event.aggregateId(), event.aggregateVersion(),
            event.schemaVersion(), record.topic(), record.partition(), record.offset(), eventAgeMs(event));

        try {
            // Kafka can deliver a message more than once. The unique event ID makes
            // processing safe to retry and prevents duplicate audit rows.
            if (repository.existsByEventId(event.eventId())) {
                log.debug("event.duplicate eventId={} aggregateId={} topic={} partition={} offset={}",
                    event.eventId(), event.aggregateId(), record.topic(), record.partition(), record.offset());
                return;
            }

            repository.save(new EngineerAudit(
                event.eventId(),
                event.eventType(),
                event.aggregateId(),
                event.occurredAt(),
                event.aggregateVersion(),
                event.schemaVersion(),
                event.payload()
            ));

            log.info("audit.persisted eventId={} eventType={} aggregateId={} aggregateVersion={} durationMs={}",
                event.eventId(), event.eventType(), event.aggregateId(), event.aggregateVersion(), durationMs(startedAt));
        } catch (RuntimeException exception) {
            log.error("event.processing.failed eventId={} eventType={} aggregateId={} aggregateVersion={} "
                    + "topic={} partition={} offset={}",
                event.eventId(), event.eventType(), event.aggregateId(), event.aggregateVersion(),
                record.topic(), record.partition(), record.offset(), exception);
            throw exception;
        }
    }

    private long eventAgeMs(SoftwareEngineerEvent event) {
        return Math.max(0, Duration.between(event.occurredAt(), Instant.now()).toMillis());
    }

    private long durationMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
