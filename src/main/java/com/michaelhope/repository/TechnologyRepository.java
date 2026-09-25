package com.michaelhope.repository;

import com.michaelhope.model.Technology;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TechnologyRepository extends JpaRepository<Technology, Integer> {

    Optional<Technology> findByNormalizedName(String normalizedName);

    List<Technology> findAllByNormalizedNameIn(Collection<String> normalizedNames);
}
