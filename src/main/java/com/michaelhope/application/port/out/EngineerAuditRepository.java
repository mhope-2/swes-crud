package com.michaelhope.application.port.out;

import com.michaelhope.domain.entity.EngineerAudit;

import java.util.List;
import java.util.UUID;

public interface EngineerAuditRepository {

    boolean existsByEventId(UUID eventId);

    EngineerAudit save(EngineerAudit audit);

    List<EngineerAudit> findByAggregateIdOrderByAggregateVersionAscOccurredAtAsc(Integer aggregateId);
}
