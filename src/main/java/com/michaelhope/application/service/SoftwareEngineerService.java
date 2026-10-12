package com.michaelhope.application.service;

import com.michaelhope.api.request.SoftwareEngineerRequest;
import com.michaelhope.api.response.SoftwareEngineerPageResponse;
import com.michaelhope.api.response.SoftwareEngineerResponse;
import com.michaelhope.application.mapper.SoftwareEngineerMapper;
import com.michaelhope.application.pagination.EngineerPage;
import com.michaelhope.application.pagination.EngineerPageQuery;
import com.michaelhope.application.port.out.EngineerResponseCache;
import com.michaelhope.application.port.out.SoftwareEngineerEventPublisher;
import com.michaelhope.application.port.out.SoftwareEngineerRepository;
import com.michaelhope.domain.entity.SoftwareEngineer;
import com.michaelhope.domain.entity.Technology;
import com.michaelhope.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SoftwareEngineerService {

    private final SoftwareEngineerRepository repository;
    private final SoftwareEngineerEventPublisher eventPublisher;
    private final TechnologyService technologyService;
    private final EngineerResponseCache engineerResponseCache;

    @Transactional(readOnly = true)
    public SoftwareEngineerPageResponse getAllSoftwareEngineers(int limit, int offset) {
        long startedAt = System.nanoTime();
        log.debug("engineers.list.started");
        EngineerPage engineerPage = repository.findPage(new EngineerPageQuery(limit, offset));
        Map<Integer, SoftwareEngineer> engineersById = engineerPage.ids().isEmpty()
            ? Map.of()
            : repository.findAllWithTechnologiesByIdIn(engineerPage.ids()).stream()
                .collect(Collectors.toMap(SoftwareEngineer::getId, Function.identity()));
        List<SoftwareEngineerResponse> responses = engineerPage.ids().stream()
            .map(engineersById::get)
            .filter(Objects::nonNull)
            .map(SoftwareEngineerMapper::toResponse)
            .toList();
        SoftwareEngineerPageResponse page = new SoftwareEngineerPageResponse(
            responses,
            limit,
            offset,
            engineerPage.total(),
            (long) offset + responses.size() < engineerPage.total()
        );
        log.debug("engineers.list.completed resultCount={} totalCount={} limit={} offset={} durationMs={}",
            responses.size(), page.total(), limit, offset, durationMs(startedAt));
        return page;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "engineerById", key = "#id")
    public SoftwareEngineerResponse getSoftwareEngineerById(Integer id) {
        SoftwareEngineerResponse response = repository.findById(id)
            .map(SoftwareEngineerMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Software engineer with id " + id + " not found"));
        log.debug("engineer.read aggregateId={}", id);
        return response;
    }

    @Transactional
    public SoftwareEngineerResponse addSoftwareEngineer(SoftwareEngineerRequest request) {
        Set<Technology> technologies = technologyService.resolve(request.technologies());
        SoftwareEngineer entity = SoftwareEngineerMapper.toEntity(request, technologies);
        entity.setAggregateVersion(1);
        SoftwareEngineer saved = repository.save(entity);
        eventPublisher.publish("software-engineer.created", saved, 1);
        log.info("engineer.created aggregateId={} aggregateVersion={}",
            saved.getId(), 1);
        log.debug("engineer.created.technology_count aggregateId={} technologyCount={}",
            saved.getId(), saved.getTechnologies().size());
        return SoftwareEngineerMapper.toResponse(saved);
    }

    @Transactional
    public SoftwareEngineerResponse updateSoftwareEngineer(Integer id, SoftwareEngineerRequest request) {
        SoftwareEngineer engineer = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Software engineer with id " + id + " not found"));
        Set<Technology> technologies = technologyService.resolve(request.technologies());
        engineer.setName(request.name());
        engineer.setTechnologies(technologies);
        int nextVersion = nextVersion(engineer);
        int previousVersion = engineer.getAggregateVersion() == null ? 0 : engineer.getAggregateVersion();
        engineer.setAggregateVersion(nextVersion);
        SoftwareEngineer saved = repository.save(engineer);
        eventPublisher.publish("software-engineer.updated", saved, nextVersion);
        SoftwareEngineerResponse response = SoftwareEngineerMapper.toResponse(saved);
        engineerResponseCache.putAfterCommit(id, response);
        log.info("engineer.updated aggregateId={} previousVersion={} aggregateVersion={}",
            saved.getId(), previousVersion, nextVersion);
        log.debug("engineer.updated.technology_count aggregateId={} technologyCount={}",
            saved.getId(), saved.getTechnologies().size());
        return response;
    }

    @Transactional
    public void deleteSoftwareEngineer(Integer id) {
        SoftwareEngineer engineer = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Software engineer with id " + id + " not found"));
        int nextVersion = nextVersion(engineer);
        repository.deleteById(id);
        eventPublisher.publish("software-engineer.deleted", engineer, nextVersion);
        engineerResponseCache.evictAfterCommit(id);
        log.info("engineer.deleted aggregateId={} aggregateVersion={}", id, nextVersion);
    }

    private int nextVersion(SoftwareEngineer engineer) {
        return (engineer.getAggregateVersion() == null ? 0 : engineer.getAggregateVersion()) + 1;
    }

    private long durationMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
