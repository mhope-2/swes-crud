package com.michaelhope.infrastructure.persistence;

import com.michaelhope.domain.entity.Technology;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TechnologyJpaRepository extends JpaRepository<Technology, Integer> {

    Optional<Technology> findByNormalizedName(String normalizedName);

    List<Technology> findAllByNormalizedNameIn(Collection<String> normalizedNames);
}
