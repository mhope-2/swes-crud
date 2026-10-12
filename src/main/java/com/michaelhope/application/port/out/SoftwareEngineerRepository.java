package com.michaelhope.application.port.out;

import com.michaelhope.application.pagination.EngineerPage;
import com.michaelhope.application.pagination.EngineerPageQuery;
import com.michaelhope.domain.entity.SoftwareEngineer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SoftwareEngineerRepository {

    EngineerPage findPage(EngineerPageQuery query);

    List<SoftwareEngineer> findAllWithTechnologiesByIdIn(Collection<Integer> ids);

    Optional<SoftwareEngineer> findById(Integer id);

    SoftwareEngineer save(SoftwareEngineer engineer);

    void deleteById(Integer id);
}
