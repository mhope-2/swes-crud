package com.michaelhope.service;

import com.michaelhope.dto.SoftwareEngineerRequest;
import com.michaelhope.dto.SoftwareEngineerResponse;
import com.michaelhope.event.SoftwareEngineerEventPublisher;
import com.michaelhope.exception.ResourceNotFoundException;
import com.michaelhope.mapper.SoftwareEngineerMapper;
import com.michaelhope.model.SoftwareEngineer;
import com.michaelhope.repository.SoftwareEngineerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SoftwareEngineerService {

    private final SoftwareEngineerRepository repository;
    private final SoftwareEngineerEventPublisher eventPublisher;

    public List<SoftwareEngineerResponse> getAllSoftwareEngineers() {
        return repository.findAll().stream()
            .map(SoftwareEngineerMapper::toResponse)
            .toList();
    }

    public SoftwareEngineerResponse getSoftwareEngineerById(Integer id) {
        return repository.findById(id)
            .map(SoftwareEngineerMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Software engineer with id " + id + " not found"));
    }

    public SoftwareEngineerResponse addSoftwareEngineer(SoftwareEngineerRequest request) {
        SoftwareEngineer entity = SoftwareEngineerMapper.toEntity(request);
        entity.setAggregateVersion(1);
        SoftwareEngineer saved = repository.save(entity);
        eventPublisher.publish("software-engineer.created", saved, 1);
        return SoftwareEngineerMapper.toResponse(saved);
    }

    public SoftwareEngineerResponse updateSoftwareEngineer(Integer id, SoftwareEngineerRequest request) {
        SoftwareEngineer engineer = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Software engineer with id " + id + " not found"));
        engineer.setName(request.name());
        engineer.setTechStack(request.techStack());
        int nextVersion = nextVersion(engineer);
        engineer.setAggregateVersion(nextVersion);
        SoftwareEngineer saved = repository.save(engineer);
        eventPublisher.publish("software-engineer.updated", saved, nextVersion);
        return SoftwareEngineerMapper.toResponse(saved);
    }

    public void deleteSoftwareEngineer(Integer id) {
        SoftwareEngineer engineer = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Software engineer with id " + id + " not found"));
        int nextVersion = nextVersion(engineer);
        repository.deleteById(id);
        eventPublisher.publish("software-engineer.deleted", engineer, nextVersion);
    }

    private int nextVersion(SoftwareEngineer engineer) {
        return (engineer.getAggregateVersion() == null ? 0 : engineer.getAggregateVersion()) + 1;
    }
}
