package com.michaelhope.api.response;

import java.util.List;

public record SoftwareEngineerResponse(
    Integer id,
    String name,
    List<String> technologies
) {}
