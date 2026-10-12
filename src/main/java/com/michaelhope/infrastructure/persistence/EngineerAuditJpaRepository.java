package com.michaelhope.infrastructure.persistence;

import com.michaelhope.domain.entity.EngineerAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EngineerAuditJpaRepository extends JpaRepository<EngineerAudit, Long> {

    boolean existsByEventId(UUID eventId);

    List<EngineerAudit> findByAggregateIdOrderByAggregateVersionAscOccurredAtAsc(Integer aggregateId);
}
