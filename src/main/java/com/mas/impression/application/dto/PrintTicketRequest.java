package com.mas.impression.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * HTTP contract for {@code POST /api/v1/print}. The frontend relays the sale and branding data
 * it already received from the backend as-is — this agent never recomputes totals or taxes.
 */
public record PrintTicketRequest(
        @NotNull @Valid SalePayload sale,
        @NotNull @Valid BrandingPayload branding) {

    public record SalePayload(
            Long saleId,
            @NotBlank String saleNumber,
            @NotEmpty List<@Valid SaleItemPayload> items,
            @NotNull List<@Valid SalePaymentPayload> payments,
            @NotNull BigDecimal subtotal,
            @NotNull BigDecimal taxTotal,
            BigDecimal discount,
            @NotNull BigDecimal total,
            String cashierName,
            String customerName,
            LocalDateTime createdAt) {

        public SalePayload {
            items = items == null ? List.of() : List.copyOf(items);
            payments = payments == null ? List.of() : List.copyOf(payments);
        }
    }

    public record SaleItemPayload(
            @NotBlank String productName,
            @NotNull BigDecimal quantity,
            @NotNull BigDecimal unitPrice,
            @NotNull BigDecimal subtotal,
            BigDecimal taxRate,
            BigDecimal taxAmount,
            BigDecimal discount) {
    }

    public record SalePaymentPayload(
            @NotBlank String paymentMethod,
            @NotNull BigDecimal amount,
            String reference) {
    }

    public record BrandingPayload(
            @NotBlank String businessName,
            String logoUrl,
            String tradeName,
            String nit,
            String address,
            String city,
            String department,
            String phone,
            String email,
            String receiptHeader,
            String receiptFooter) {
    }
}
