package com.greateastern.warehouse.sale.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(name = "CreateSaleRequest")
public record CreateSaleRequest(
    @Schema(example = "", defaultValue = "")
    String reference,
    @Schema(
        example = "[{\"variantId\":2001,\"quantity\":1}]",
        defaultValue = "[{\"variantId\":2001,\"quantity\":1}]"
    )
    @NotEmpty(message = "Sale lines are required")
    List<@Valid CreateSaleLineRequest> lines
) {
}
