package com.michaelhope.dto;

import jakarta.validation.constraints.NotBlank;

public record SoftwareEngineerRequest(
    @NotBlank String name,
    @NotBlank String techStack
) {}
