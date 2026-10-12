package com.michaelhope.application.port.out;

import com.michaelhope.domain.entity.Technology;

import java.util.Collection;
import java.util.List;

public interface TechnologyRepository {

    List<Technology> findAllByNormalizedNameIn(Collection<String> normalizedNames);

    Technology save(Technology technology);
}
