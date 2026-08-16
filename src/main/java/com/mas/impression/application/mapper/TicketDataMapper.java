package com.mas.impression.application.mapper;

import com.mas.impression.application.dto.PrintTicketRequest;
import com.mas.impression.domain.model.BrandingData;
import com.mas.impression.domain.model.SaleData;
import com.mas.impression.domain.model.SaleItemData;
import com.mas.impression.domain.model.SalePaymentData;
import com.mas.impression.domain.model.TicketData;

/**
 * Converts the HTTP-facing {@link PrintTicketRequest} into the framework-free
 * {@link TicketData} the domain formatter works with, keeping validation annotations and
 * wire-format concerns out of the domain package.
 */
public final class TicketDataMapper {

    private TicketDataMapper() {
    }

    public static TicketData toDomain(PrintTicketRequest request) {
        return new TicketData(toSaleData(request.sale()), toBrandingData(request.branding()));
    }

    private static SaleData toSaleData(PrintTicketRequest.SalePayload payload) {
        return new SaleData(
                payload.saleNumber(),
                payload.items().stream().map(TicketDataMapper::toItemData).toList(),
                payload.payments().stream().map(TicketDataMapper::toPaymentData).toList(),
                payload.subtotal(),
                payload.taxTotal(),
                payload.discount(),
                payload.total(),
                payload.cashierName(),
                payload.customerName(),
                payload.createdAt());
    }

    private static SaleItemData toItemData(PrintTicketRequest.SaleItemPayload payload) {
        return new SaleItemData(
                payload.productName(),
                payload.quantity(),
                payload.unitPrice(),
                payload.subtotal(),
                payload.taxRate(),
                payload.taxAmount(),
                payload.discount());
    }

    private static SalePaymentData toPaymentData(PrintTicketRequest.SalePaymentPayload payload) {
        return new SalePaymentData(payload.paymentMethod(), payload.amount(), payload.reference());
    }

    private static BrandingData toBrandingData(PrintTicketRequest.BrandingPayload payload) {
        return new BrandingData(
                payload.businessName(),
                payload.tradeName(),
                payload.nit(),
                payload.address(),
                payload.city(),
                payload.department(),
                payload.phone(),
                payload.email(),
                payload.receiptHeader(),
                payload.receiptFooter());
    }
}
