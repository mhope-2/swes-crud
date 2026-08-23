package com.michaelhope.consumer;

import com.michaelhope.event.KafkaTopics;
import com.michaelhope.event.SoftwareEngineerEvent;
import com.michaelhope.model.EngineerAudit;
import com.michaelhope.repository.EngineerAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EngineerAuditConsumer {

    private final EngineerAuditRepository repository;

    @KafkaListener(topics = KafkaTopics.SOFTWARE_ENGINEER_EVENTS_V1)
    public void consume(SoftwareEngineerEvent event) {
        // Kafka can deliver a message more than once. The unique event ID makes
        // processing safe to retry and prevents duplicate audit rows.
        if (repository.existsByEventId(event.eventId())) {
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
    }
}
