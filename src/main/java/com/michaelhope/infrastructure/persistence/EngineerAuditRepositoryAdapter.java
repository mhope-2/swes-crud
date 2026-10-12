package com.michaelhope.infrastructure.persistence;

import com.michaelhope.application.port.out.EngineerAuditRepository;
import com.michaelhope.domain.entity.EngineerAudit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EngineerAuditRepositoryAdapter implements EngineerAuditRepository {

    private final EngineerAuditJpaRepository repository;

    @Override
    public boolean existsByEventId(UUID eventId) {
        return repository.existsByEventId(eventId);
    }

    @Override
    public EngineerAudit save(EngineerAudit audit) {
        return repository.save(audit);
    }

    @Override
    public List<EngineerAudit> findByAggregateIdOrderByAggregateVersionAscOccurredAtAsc(Integer aggregateId) {
        return repository.findByAggregateIdOrderByAggregateVersionAscOccurredAtAsc(aggregateId);
    }
}
