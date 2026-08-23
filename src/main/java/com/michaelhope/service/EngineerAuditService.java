package com.michaelhope.service;

import com.michaelhope.dto.EngineerAuditResponse;
import com.michaelhope.repository.EngineerAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EngineerAuditService {

    private final EngineerAuditRepository repository;

    public List<EngineerAuditResponse> getHistory(Integer aggregateId) {
        return repository.findByAggregateIdOrderByAggregateVersionAscOccurredAtAsc(aggregateId)
            .stream()
            .map(EngineerAuditResponse::from)
            .toList();
    }
}
