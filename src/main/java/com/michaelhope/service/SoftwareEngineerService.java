package com.michaelhope.service;

import com.michaelhope.dto.SoftwareEngineerRequest;
import com.michaelhope.dto.SoftwareEngineerResponse;
import com.michaelhope.event.SoftwareEngineerEventPublisher;
import com.michaelhope.exception.ResourceNotFoundException;
import com.michaelhope.mapper.SoftwareEngineerMapper;
import com.michaelhope.model.SoftwareEngineer;
import com.michaelhope.repository.SoftwareEngineerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class SoftwareEngineerService {

    private final SoftwareEngineerRepository repository;
    private final SoftwareEngineerEventPublisher eventPublisher;

    public List<SoftwareEngineerResponse> getAllSoftwareEngineers() {
        long startedAt = System.nanoTime();
        log.debug("engineers.list.started");
        List<SoftwareEngineerResponse> responses = repository.findAll().stream()
            .map(SoftwareEngineerMapper::toResponse)
            .toList();
        log.debug("engineers.list.completed resultCount={} durationMs={}",
            responses.size(), durationMs(startedAt));
        return responses;
    }

    public SoftwareEngineerResponse getSoftwareEngineerById(Integer id) {
        SoftwareEngineerResponse response = repository.findById(id)
            .map(SoftwareEngineerMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Software engineer with id " + id + " not found"));
        log.debug("engineer.read aggregateId={}", id);
        return response;
    }

    public SoftwareEngineerResponse addSoftwareEngineer(SoftwareEngineerRequest request) {
        SoftwareEngineer entity = SoftwareEngineerMapper.toEntity(request);
        entity.setAggregateVersion(1);
        SoftwareEngineer saved = repository.save(entity);
        eventPublisher.publish("software-engineer.created", saved, 1);
        log.info("engineer.created aggregateId={} aggregateVersion={}",
            saved.getId(), 1);
        return SoftwareEngineerMapper.toResponse(saved);
    }

    public SoftwareEngineerResponse updateSoftwareEngineer(Integer id, SoftwareEngineerRequest request) {
        SoftwareEngineer engineer = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Software engineer with id " + id + " not found"));
        engineer.setName(request.name());
        engineer.setTechStack(request.techStack());
        int nextVersion = nextVersion(engineer);
        int previousVersion = engineer.getAggregateVersion() == null ? 0 : engineer.getAggregateVersion();
        engineer.setAggregateVersion(nextVersion);
        SoftwareEngineer saved = repository.save(engineer);
        eventPublisher.publish("software-engineer.updated", saved, nextVersion);
        log.info("engineer.updated aggregateId={} previousVersion={} aggregateVersion={}",
            saved.getId(), previousVersion, nextVersion);
        return SoftwareEngineerMapper.toResponse(saved);
    }

    public void deleteSoftwareEngineer(Integer id) {
        SoftwareEngineer engineer = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Software engineer with id " + id + " not found"));
        int nextVersion = nextVersion(engineer);
        repository.deleteById(id);
        eventPublisher.publish("software-engineer.deleted", engineer, nextVersion);
        log.info("engineer.deleted aggregateId={} aggregateVersion={}", id, nextVersion);
    }

    private int nextVersion(SoftwareEngineer engineer) {
        return (engineer.getAggregateVersion() == null ? 0 : engineer.getAggregateVersion()) + 1;
    }

    private long durationMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
