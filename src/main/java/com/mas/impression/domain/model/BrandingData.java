package com.mas.impression.domain.model;

/**
 * Tenant business/branding text printed on the ticket header and footer. Relayed by the
 * frontend on every print request rather than cached by the agent, since it is tenant-editable
 * business data the agent has no business owning a stale copy of.
 */
public record BrandingData(
        String businessName,
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
