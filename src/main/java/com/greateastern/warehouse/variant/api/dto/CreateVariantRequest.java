package com.greateastern.warehouse.variant.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(name = "CreateVariantRequest")
public record CreateVariantRequest(
    @Schema(example = "MIG-DEMO-NEW-44", defaultValue = "MIG-DEMO-NEW-44")
    @NotBlank(message = "Variant SKU is required")
    @Size(max = 100, message = "Variant SKU must not exceed 100 characters")
    String sku,
    @Schema(example = "Migration Demo Variant 44", defaultValue = "Migration Demo Variant 44")
    @NotBlank(message = "Variant name is required")
    @Size(max = 160, message = "Variant name must not exceed 160 characters")
    String name,
    @Schema(example = "149.90", defaultValue = "149.90")
    @NotNull(message = "Variant price is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Variant price must be greater than zero")
    BigDecimal price,
    @Schema(example = "15", defaultValue = "15")
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be zero or greater")
    Integer stockQuantity,
    @Schema(example = "true", defaultValue = "true")
    Boolean active
) {
}
