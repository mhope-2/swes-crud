package com.michaelhope.application.mapper;

import com.michaelhope.api.request.SoftwareEngineerRequest;
import com.michaelhope.api.response.SoftwareEngineerResponse;
import com.michaelhope.domain.entity.SoftwareEngineer;
import com.michaelhope.domain.entity.Technology;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

@NoArgsConstructor
public class SoftwareEngineerMapper {

    public static SoftwareEngineerResponse toResponse (SoftwareEngineer entity) {
        return new SoftwareEngineerResponse(
            entity.getId(),
            entity.getName(),
            entity.getTechnologies().stream()
                .map(Technology::getName)
                .sorted(Comparator.comparing(value -> value.toLowerCase(Locale.ROOT)))
                .toList()
        );
    }

    public static SoftwareEngineer toEntity(SoftwareEngineerRequest request, Set<Technology> technologies) {
        SoftwareEngineer entity = new SoftwareEngineer();
        entity.setName(request.name());
        entity.setTechnologies(technologies);
        return entity;
    }
}
