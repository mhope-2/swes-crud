package com.michaelhope.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SoftwareEngineerRequest(
    @NotBlank String name,
    @NotEmpty(message = "At least one technology is required")
    List<@NotBlank @Size(max = 100) String> technologies
) {}
