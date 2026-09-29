package com.michaelhope.repository;

import com.michaelhope.model.SoftwareEngineer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SoftwareEngineerRepository extends JpaRepository<SoftwareEngineer, Integer> {

    @Override
    @EntityGraph(attributePaths = "technologies")
    List<SoftwareEngineer> findAll();

    @Query("select engineer.id from SoftwareEngineer engineer order by engineer.id")
    Page<Integer> findIds(Pageable pageable);

    @EntityGraph(attributePaths = "technologies")
    @Query("select distinct engineer from SoftwareEngineer engineer where engineer.id in :ids")
    List<SoftwareEngineer> findAllWithTechnologiesByIdIn(@Param("ids") Collection<Integer> ids);

    @Override
    @EntityGraph(attributePaths = "technologies")
    Optional<SoftwareEngineer> findById(Integer id);
}
