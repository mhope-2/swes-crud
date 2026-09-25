package com.michaelhope.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michaelhope.model.SoftwareEngineer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SoftwareEngineerEventPublisherTest {

    @Test
    void publishesAndHandlesSuccessfulBrokerAcknowledgment() {
        KafkaTemplate<String, SoftwareEngineerEvent> kafkaTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper();
        CompletableFuture<SendResult<String, SoftwareEngineerEvent>> future = new CompletableFuture<>();
        SendResult<String, SoftwareEngineerEvent> result = mock(SendResult.class);
        RecordMetadata metadata = new RecordMetadata(null, 2, 17, 0, 0, 0);

        when(kafkaTemplate.send(eq(KafkaTopics.SOFTWARE_ENGINEER_EVENTS_V2), eq("1"),
            org.mockito.ArgumentMatchers.any(SoftwareEngineerEvent.class))).thenReturn(future);
        when(result.getRecordMetadata()).thenReturn(metadata);

        SoftwareEngineerEventPublisher publisher = new SoftwareEngineerEventPublisher(kafkaTemplate, objectMapper);
        publisher.publish("software-engineer.created", new SoftwareEngineer(1, "Alice", "Java"), 1);
        future.complete(result);

        var eventCaptor = forClass(SoftwareEngineerEvent.class);
        verify(kafkaTemplate).send(eq(KafkaTopics.SOFTWARE_ENGINEER_EVENTS_V2), eq("1"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().schemaVersion()).isEqualTo(SoftwareEngineerEvent.CURRENT_SCHEMA_VERSION);
        assertThat(eventCaptor.getValue().payload()).contains("\"technologies\":[\"Java\"]");
    }

    @Test
    void handlesFailedBrokerAcknowledgmentWithoutThrowingFromCallback() {
        KafkaTemplate<String, SoftwareEngineerEvent> kafkaTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper();
        CompletableFuture<SendResult<String, SoftwareEngineerEvent>> future = new CompletableFuture<>();

        when(kafkaTemplate.send(eq(KafkaTopics.SOFTWARE_ENGINEER_EVENTS_V2), eq("1"),
            org.mockito.ArgumentMatchers.any(SoftwareEngineerEvent.class))).thenReturn(future);

        SoftwareEngineerEventPublisher publisher = new SoftwareEngineerEventPublisher(kafkaTemplate, objectMapper);
        publisher.publish("software-engineer.created", new SoftwareEngineer(1, "Alice", "Java"), 1);
        future.completeExceptionally(new IllegalStateException("broker unavailable"));

        verify(kafkaTemplate).send(eq(KafkaTopics.SOFTWARE_ENGINEER_EVENTS_V2), eq("1"),
            any(SoftwareEngineerEvent.class));
    }
}
