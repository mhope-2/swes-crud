package com.michaelhope.repository;

import com.michaelhope.model.SoftwareEngineer;
import com.michaelhope.model.Technology;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class SoftwareEngineerRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private SoftwareEngineerRepository engineerRepository;

    @Autowired
    private TechnologyRepository technologyRepository;

    @Test
    void findAllLoadsTechnologiesThroughEntityGraph() {
        Technology java = technologyRepository.save(new Technology("Java", "java"));
        Technology spring = technologyRepository.save(new Technology("Spring", "spring"));
        SoftwareEngineer engineer = new SoftwareEngineer(
            null,
            "Alice",
            new LinkedHashSet<>(Set.of(java, spring))
        );
        engineer.setAggregateVersion(1);
        engineerRepository.saveAndFlush(engineer);

        List<SoftwareEngineer> engineers = engineerRepository.findAll();

        assertThat(engineers).hasSize(1);
        assertThat(engineers.getFirst().getTechnologies())
            .extracting(Technology::getNormalizedName)
            .containsExactlyInAnyOrder("java", "spring");
    }

    @Test
    void findByIdReturnsEngineerWithoutLazyLoadingFailure() {
        Technology java = technologyRepository.save(new Technology("Java", "java"));
        SoftwareEngineer engineer = new SoftwareEngineer(null, "Bob", Set.of(java));
        engineer.setAggregateVersion(1);
        Integer id = engineerRepository.saveAndFlush(engineer).getId();

        engineerRepository.flush();
        SoftwareEngineer loaded = engineerRepository.findById(id).orElseThrow();

        assertThat(loaded.getName()).isEqualTo("Bob");
        assertThat(loaded.getTechnologies())
            .extracting(Technology::getName)
            .containsExactly("Java");
    }
}
