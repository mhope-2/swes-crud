package com.michaelhope.service;

import com.michaelhope.dto.SoftwareEngineerRequest;
import com.michaelhope.dto.SoftwareEngineerResponse;
import com.michaelhope.exception.ResourceNotFoundException;
import com.michaelhope.mapper.SoftwareEngineerMapper;
import com.michaelhope.model.SoftwareEngineer;
import com.michaelhope.repository.SoftwareEngineerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SoftwareEngineerService {

    private final SoftwareEngineerRepository repository;

    public SoftwareEngineerService(SoftwareEngineerRepository repository) {
        this.repository = repository;
    }

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
        SoftwareEngineer saved = repository.save(SoftwareEngineerMapper.toEntity(request));
        return SoftwareEngineerMapper.toResponse(saved);
    }

    public SoftwareEngineerResponse updateSoftwareEngineer(Integer id, SoftwareEngineerRequest request) {
        SoftwareEngineer engineer = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Software engineer with id " + id + " not found"));
        engineer.setName(request.name());
        engineer.setTechStack(request.techStack());
        return SoftwareEngineerMapper.toResponse(repository.save(engineer));
    }

    public void deleteSoftwareEngineer(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Software engineer with id " + id + " not found");
        }
        repository.deleteById(id);
    }
}
