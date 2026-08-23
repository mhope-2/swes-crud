package com.michaelhope.repository;

import com.michaelhope.model.EngineerAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EngineerAuditRepository extends JpaRepository<EngineerAudit, Long> {

    boolean existsByEventId(UUID eventId);

    List<EngineerAudit> findByAggregateIdOrderByAggregateVersionAscOccurredAtAsc(Integer aggregateId);
}
