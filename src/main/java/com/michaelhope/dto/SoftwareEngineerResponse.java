package com.michaelhope.dto;

import java.util.List;

public record SoftwareEngineerResponse(
    Integer id,
    String name,
    List<String> technologies
) {}
