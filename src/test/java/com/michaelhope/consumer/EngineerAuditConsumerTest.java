package com.michaelhope.consumer;

import com.michaelhope.event.SoftwareEngineerEvent;
import com.michaelhope.repository.EngineerAuditRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EngineerAuditConsumerTest {

    private static final String TOPIC = "software-engineer.events.v1";

    @Test
    void persistsNewEvent() {
        EngineerAuditRepository repository = mock(EngineerAuditRepository.class);
        EngineerAuditConsumer consumer = new EngineerAuditConsumer(repository);
        SoftwareEngineerEvent event = event();
        ConsumerRecord<String, SoftwareEngineerEvent> record = new ConsumerRecord<>(TOPIC, 0, 5L, "1", event);
        when(repository.existsByEventId(event.eventId())).thenReturn(false);

        consumer.consume(record);

        verify(repository).save(any());
    }

    @Test
    void skipsDuplicateEvent() {
        EngineerAuditRepository repository = mock(EngineerAuditRepository.class);
        EngineerAuditConsumer consumer = new EngineerAuditConsumer(repository);
        SoftwareEngineerEvent event = event();
        ConsumerRecord<String, SoftwareEngineerEvent> record = new ConsumerRecord<>(TOPIC, 0, 5L, "1", event);
        when(repository.existsByEventId(event.eventId())).thenReturn(true);

        consumer.consume(record);

        verify(repository).existsByEventId(event.eventId());
        verify(repository, never()).save(any());
    }

    @Test
    void rethrowsPersistenceFailureForKafkaErrorHandling() {
        EngineerAuditRepository repository = mock(EngineerAuditRepository.class);
        EngineerAuditConsumer consumer = new EngineerAuditConsumer(repository);
        SoftwareEngineerEvent event = event();
        ConsumerRecord<String, SoftwareEngineerEvent> record = new ConsumerRecord<>(TOPIC, 0, 5L, "1", event);
        when(repository.existsByEventId(event.eventId())).thenReturn(false);
        when(repository.save(any())).thenThrow(new IllegalStateException("database unavailable"));

        assertThatThrownBy(() -> consumer.consume(record))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("database unavailable");
    }

    private SoftwareEngineerEvent event() {
        return new SoftwareEngineerEvent(
            UUID.randomUUID(),
            "software-engineer.created",
            1,
            Instant.now(),
            1,
            "{\"id\":1}",
            1
        );
    }

}
