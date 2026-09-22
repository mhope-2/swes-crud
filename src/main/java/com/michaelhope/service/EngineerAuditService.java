package com.michaelhope.service;

import com.michaelhope.dto.EngineerAuditResponse;
import com.michaelhope.repository.EngineerAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EngineerAuditService {

    private final EngineerAuditRepository repository;

    public List<EngineerAuditResponse> getHistory(Integer aggregateId) {
        List<EngineerAuditResponse> history = repository.findByAggregateIdOrderByAggregateVersionAscOccurredAtAsc(aggregateId)
            .stream()
            .map(EngineerAuditResponse::from)
            .toList();
        log.debug("engineer.history.read aggregateId={} resultCount={}", aggregateId, history.size());
        return history;
    }
}
