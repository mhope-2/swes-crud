package com.michaelhope.mapper;

import com.michaelhope.dto.SoftwareEngineerRequest;
import com.michaelhope.dto.SoftwareEngineerResponse;
import com.michaelhope.model.SoftwareEngineer;

public class SoftwareEngineerMapper {

    private SoftwareEngineerMapper() {}

    public static SoftwareEngineerResponse toResponse(SoftwareEngineer entity) {
        return new SoftwareEngineerResponse(entity.getId(), entity.getName(), entity.getTechStack());
    }

    public static SoftwareEngineer toEntity(SoftwareEngineerRequest request) {
        SoftwareEngineer entity = new SoftwareEngineer();
        entity.setName(request.name());
        entity.setTechStack(request.techStack());
        return entity;
    }
}
