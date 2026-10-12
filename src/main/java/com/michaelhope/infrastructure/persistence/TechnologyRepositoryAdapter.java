package com.michaelhope.infrastructure.persistence;

import com.michaelhope.application.port.out.TechnologyRepository;
import com.michaelhope.domain.entity.Technology;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TechnologyRepositoryAdapter implements TechnologyRepository {

    private final TechnologyJpaRepository repository;

    @Override
    public List<Technology> findAllByNormalizedNameIn(Collection<String> normalizedNames) {
        return repository.findAllByNormalizedNameIn(normalizedNames);
    }

    @Override
    public Technology save(Technology technology) {
        return repository.save(technology);
    }
}
