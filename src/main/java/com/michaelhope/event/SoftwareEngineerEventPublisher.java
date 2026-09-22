package com.michaelhope.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michaelhope.model.SoftwareEngineer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SoftwareEngineerEventPublisher {

    private final KafkaTemplate<String, SoftwareEngineerEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String eventType, SoftwareEngineer engineer, int aggregateVersion) {
        SoftwareEngineerEvent event = new SoftwareEngineerEvent(
            UUID.randomUUID(),
            eventType,
            engineer.getId(),
            Instant.now(),
            aggregateVersion,
            payloadFor(engineer),
            1
        );

        // The engineer ID is the key, so Kafka keeps one engineer's events ordered.
        String topic = KafkaTopics.SOFTWARE_ENGINEER_EVENTS_V1;
        String key = String.valueOf(engineer.getId());
        long startedAt = System.nanoTime();

        log.debug("event.created eventId={} eventType={} aggregateId={} aggregateVersion={} schemaVersion={} topic={}",
            event.eventId(), event.eventType(), event.aggregateId(), event.aggregateVersion(),
            event.schemaVersion(), topic);
        log.debug("event.send.started eventId={} topic={} key={}", event.eventId(), topic, key);

        try {
            kafkaTemplate.send(topic, key, event)
                .whenComplete((result, failure) -> logSendResult(event, topic, startedAt, result, failure));
        } catch (RuntimeException exception) {
            log.error("event.send.failed eventId={} eventType={} aggregateId={} topic={}",
                event.eventId(), event.eventType(), event.aggregateId(), topic, exception);
            throw exception;
        }
    }

    private void logSendResult(
            SoftwareEngineerEvent event,
            String topic,
            long startedAt,
            SendResult<String, SoftwareEngineerEvent> result,
            Throwable failure) {
        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        if (failure != null) {
            log.error("event.send.failed eventId={} eventType={} aggregateId={} topic={} durationMs={}",
                event.eventId(), event.eventType(), event.aggregateId(), topic, durationMs, failure);
            return;
        }

        var metadata = result.getRecordMetadata();
        log.info("event.send.succeeded eventId={} eventType={} aggregateId={} topic={} partition={} offset={} durationMs={}",
            event.eventId(), event.eventType(), event.aggregateId(), topic,
            metadata.partition(), metadata.offset(), durationMs);
    }

    private String payloadFor(SoftwareEngineer engineer) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                "id", engineer.getId(),
                "name", engineer.getName(),
                "techStack", engineer.getTechStack()
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize software engineer event payload", exception);
        }
    }
}
