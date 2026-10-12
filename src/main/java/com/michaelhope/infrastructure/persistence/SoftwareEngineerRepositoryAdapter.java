package com.michaelhope.infrastructure.persistence;

import com.michaelhope.application.pagination.EngineerPage;
import com.michaelhope.application.pagination.EngineerPageQuery;
import com.michaelhope.application.port.out.SoftwareEngineerRepository;
import com.michaelhope.domain.entity.SoftwareEngineer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SoftwareEngineerRepositoryAdapter implements SoftwareEngineerRepository {

    private final SoftwareEngineerJpaRepository repository;

    @Override
    public EngineerPage findPage(EngineerPageQuery query) {
        var page = repository.findIds(new OffsetLimitPageable(query.offset(), query.limit()));
        return new EngineerPage(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<SoftwareEngineer> findAllWithTechnologiesByIdIn(Collection<Integer> ids) {
        return repository.findAllWithTechnologiesByIdIn(ids);
    }

    @Override
    public Optional<SoftwareEngineer> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public SoftwareEngineer save(SoftwareEngineer engineer) {
        return repository.save(engineer);
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}
