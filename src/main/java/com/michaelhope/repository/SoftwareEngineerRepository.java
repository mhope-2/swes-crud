package com.michaelhope.repository;

import com.michaelhope.model.SoftwareEngineer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SoftwareEngineerRepository extends JpaRepository<SoftwareEngineer, Integer> {

    @Override
    @EntityGraph(attributePaths = "technologies")
    List<SoftwareEngineer> findAll();

    @Override
    @EntityGraph(attributePaths = "technologies")
    Optional<SoftwareEngineer> findById(Integer id);
}
