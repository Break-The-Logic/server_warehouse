package com.greateastern.warehouse.item.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateItemRequest")
public record CreateItemRequest(
    @Schema(example = "Migration Demo Item", defaultValue = "Migration Demo Item")
    @NotBlank(message = "Item name is required")
    @Size(max = 120, message = "Item name must not exceed 120 characters")
    String name,
    @Schema(
        example = "Item created from Swagger using default request body",
        defaultValue = "Item created from Swagger using default request body"
    )
    @NotBlank(message = "Item description is required")
    @Size(max = 2000, message = "Item description must not exceed 2000 characters")
    String description,
    @Schema(example = "true", defaultValue = "true")
    Boolean active
) {
}
