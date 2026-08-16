package com.mas.impression.domain.model;

import java.math.BigDecimal;

/**
 * A payment applied to the sale, as already recorded by the backend.
 */
public record SalePaymentData(
        String paymentMethod,
        BigDecimal amount,
        String reference) {
}
