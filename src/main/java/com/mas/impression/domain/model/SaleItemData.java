package com.mas.impression.domain.model;

import java.math.BigDecimal;

/**
 * A single sold line as already computed and confirmed by the backend. The formatter must not
 * recompute any of these values, only lay them out.
 */
public record SaleItemData(
        String productName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal discount) {
}
