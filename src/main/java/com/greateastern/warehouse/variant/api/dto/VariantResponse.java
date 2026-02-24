package com.greateastern.warehouse.variant.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record VariantResponse(
    Long id,
    Long itemId,
    String sku,
    String name,
    BigDecimal price,
    Integer stockQuantity,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
}
