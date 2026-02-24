package com.greateastern.warehouse.sale.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleResponse(
    Long id,
    String reference,
    BigDecimal totalAmount,
    Instant createdAt,
    List<SaleLineResponse> lines
) {
}
