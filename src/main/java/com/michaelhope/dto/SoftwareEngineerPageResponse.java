package com.michaelhope.dto;

import java.util.List;

public record SoftwareEngineerPageResponse(
    List<SoftwareEngineerResponse> items,
    int limit,
    int offset,
    long total,
    boolean hasNext
) {
}
