package com.greateastern.warehouse.item.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "UpdateItemRequest")
public record UpdateItemRequest(
    @Schema(example = "Migration Seed Item A Updated", defaultValue = "Migration Seed Item A Updated")
    @NotBlank(message = "Item name is required")
    @Size(max = 120, message = "Item name must not exceed 120 characters")
    String name,
    @Schema(
        example = "Updated item description from Swagger default payload",
        defaultValue = "Updated item description from Swagger default payload"
    )
    @NotBlank(message = "Item description is required")
    @Size(max = 2000, message = "Item description must not exceed 2000 characters")
    String description,
    @Schema(example = "true", defaultValue = "true")
    Boolean active
) {
}
