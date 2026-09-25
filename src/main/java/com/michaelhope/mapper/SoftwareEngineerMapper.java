package com.michaelhope.mapper;

import com.michaelhope.dto.SoftwareEngineerRequest;
import com.michaelhope.dto.SoftwareEngineerResponse;
import com.michaelhope.model.SoftwareEngineer;
import com.michaelhope.model.Technology;
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
