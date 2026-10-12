package com.michaelhope.application.pagination;

public record EngineerPageQuery(int limit, int offset) {

    public EngineerPageQuery {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must be non-negative");
        }
    }
}
