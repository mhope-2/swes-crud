package com.michaelhope.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michaelhope.model.SoftwareEngineer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
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
        kafkaTemplate.send(
            KafkaTopics.SOFTWARE_ENGINEER_EVENTS_V1,
            String.valueOf(engineer.getId()),
            event
        );
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
