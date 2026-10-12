package com.michaelhope.application.mapper;

import com.michaelhope.api.request.SoftwareEngineerRequest;
import com.michaelhope.api.response.SoftwareEngineerResponse;
import com.michaelhope.domain.entity.SoftwareEngineer;
import com.michaelhope.domain.entity.Technology;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SoftwareEngineerMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        SoftwareEngineer entity = new SoftwareEngineer(1, "Alice", Set.of(
            new Technology(1, "Java", "java"),
            new Technology(2, "Spring", "spring")
        ));

        SoftwareEngineerResponse response = SoftwareEngineerMapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.technologies()).containsExactly("Java", "Spring");
    }

    @Test
    void toEntity_mapsNameAndTechnologies() {
        SoftwareEngineerRequest request = new SoftwareEngineerRequest("Bob", List.of("Kotlin"));
        Set<Technology> technologies = new LinkedHashSet<>(Set.of(
            new Technology(1, "Kotlin", "kotlin")
        ));

        SoftwareEngineer entity = SoftwareEngineerMapper.toEntity(request, technologies);

        assertThat(entity.getName()).isEqualTo("Bob");
        assertThat(entity.getTechnologies()).containsExactlyElementsOf(technologies);
        assertThat(entity.getId()).isNull();
    }
}
