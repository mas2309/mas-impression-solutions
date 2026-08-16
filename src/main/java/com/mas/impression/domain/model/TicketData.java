package com.mas.impression.domain.model;

/**
 * Everything {@link com.mas.impression.domain.service.TicketFormatter} needs to lay out one
 * ticket: the sale to print and the tenant branding to stamp on it.
 */
public record TicketData(SaleData sale, BrandingData branding) {
}
