package com.greateastern.warehouse.variant.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(name = "UpdateVariantRequest")
public record UpdateVariantRequest(
    @Schema(example = "MIG-SEED-A-RED-42", defaultValue = "MIG-SEED-A-RED-42")
    @NotBlank(message = "Variant SKU is required")
    @Size(max = 100, message = "Variant SKU must not exceed 100 characters")
    String sku,
    @Schema(example = "Seed Variant A Red 42 Updated", defaultValue = "Seed Variant A Red 42 Updated")
    @NotBlank(message = "Variant name is required")
    @Size(max = 160, message = "Variant name must not exceed 160 characters")
    String name,
    @Schema(example = "131.90", defaultValue = "131.90")
    @NotNull(message = "Variant price is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Variant price must be greater than zero")
    BigDecimal price,
    @Schema(example = "45", defaultValue = "45")
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be zero or greater")
    Integer stockQuantity,
    @Schema(example = "true", defaultValue = "true")
    Boolean active
) {
}
