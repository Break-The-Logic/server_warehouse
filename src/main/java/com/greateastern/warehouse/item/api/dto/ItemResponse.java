package com.greateastern.warehouse.item.api.dto;

import java.time.Instant;

public record ItemResponse(
    Long id,
    String name,
    String description,
    boolean active,
    Instant createdAt,
    Instant updatedAt,
    int variantCount
) {
}
