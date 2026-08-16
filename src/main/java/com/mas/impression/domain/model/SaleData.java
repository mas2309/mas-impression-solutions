package com.mas.impression.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The authoritative sale as returned by the backend right after {@code POST /api/v1/sales}.
 * Totals and taxes arrive already computed; this agent only formats and prints them.
 */
public record SaleData(
        String saleNumber,
        List<SaleItemData> items,
        List<SalePaymentData> payments,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal discount,
        BigDecimal total,
        String cashierName,
        String customerName,
        LocalDateTime createdAt) {

    public SaleData {
        items = items == null ? List.of() : List.copyOf(items);
        payments = payments == null ? List.of() : List.copyOf(payments);
    }
}
