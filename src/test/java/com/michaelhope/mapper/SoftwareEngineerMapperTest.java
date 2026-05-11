package com.michaelhope.mapper;

import com.michaelhope.dto.SoftwareEngineerRequest;
import com.michaelhope.dto.SoftwareEngineerResponse;
import com.michaelhope.model.SoftwareEngineer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SoftwareEngineerMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        SoftwareEngineer entity = new SoftwareEngineer(1, "Alice", "Java");

        SoftwareEngineerResponse response = SoftwareEngineerMapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.techStack()).isEqualTo("Java");
    }

    @Test
    void toEntity_mapsNameAndTechStack() {
        SoftwareEngineerRequest request = new SoftwareEngineerRequest("Bob", "Kotlin");

        SoftwareEngineer entity = SoftwareEngineerMapper.toEntity(request);

        assertThat(entity.getName()).isEqualTo("Bob");
        assertThat(entity.getTechStack()).isEqualTo("Kotlin");
        assertThat(entity.getId()).isNull();
    }
}
