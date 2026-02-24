package com.greateastern.warehouse.sale.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CreateSaleLineRequest")
public record CreateSaleLineRequest(
    @Schema(example = "2001", defaultValue = "2001")
    @NotNull(message = "Variant id is required")
    Long variantId,
    @Schema(example = "1", defaultValue = "1")
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity
) {
}
