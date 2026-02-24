package com.greateastern.warehouse.sale.api.dto;

import java.math.BigDecimal;

public record SaleLineResponse(
    Long id,
    Long variantId,
    String sku,
    String variantName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
) {
}
