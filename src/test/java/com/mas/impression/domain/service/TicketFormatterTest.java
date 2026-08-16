package com.mas.impression.domain.service;

import com.mas.impression.domain.model.BrandingData;
import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.SaleData;
import com.mas.impression.domain.model.SaleItemData;
import com.mas.impression.domain.model.SalePaymentData;
import com.mas.impression.domain.model.TicketData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketFormatterTest {

    private static final byte[] NO_LOGO = new byte[0];

    private final TicketFormatter formatter = new TicketFormatter();

    @Test
    void includesLogoRasterRightAfterAligningCenter() {
        byte[] logo = {1, 2, 3, 4};

        byte[] ticket = formatter.format(sampleTicket(), PaperWidth.MM_58, logo);

        byte[] expected = concat(EscPosCommands.ALIGN_CENTER, logo);
        byte[] actual = new byte[expected.length];
        System.arraycopy(ticket, EscPosCommands.INIT.length + EscPosCommands.SELECT_CODEPAGE_850.length,
                actual, 0, expected.length);
        assertTrue(Arrays.equals(expected, actual));
    }

    @Test
    void omitsLogoBytesWhenNoneProvided() {
        String decoded = decode(formatTicket(sampleTicket(), PaperWidth.MM_58));

        assertTrue(decoded.contains("Tienda Amara"));
    }

    @Test
    void startsWithInitAndCodepageSelection() {
        byte[] ticket = formatTicket(sampleTicket(), PaperWidth.MM_58);
        byte[] expectedStart = concat(EscPosCommands.INIT, EscPosCommands.SELECT_CODEPAGE_850);

        byte[] actualStart = new byte[expectedStart.length];
        System.arraycopy(ticket, 0, actualStart, 0, expectedStart.length);
        assertTrue(Arrays.equals(expectedStart, actualStart));
    }

    @Test
    void endsWithPartialCutCommand() {
        byte[] ticket = formatTicket(sampleTicket(), PaperWidth.MM_58);

        byte[] tail = new byte[EscPosCommands.PARTIAL_CUT.length];
        System.arraycopy(ticket, ticket.length - tail.length, tail, 0, tail.length);
        assertTrue(Arrays.equals(EscPosCommands.PARTIAL_CUT, tail));
    }

    @Test
    void includesBrandingHeaderFields() {
        String decoded = decode(formatTicket(sampleTicket(), PaperWidth.MM_58));

        assertTrue(decoded.contains("Tienda Amara"));
        assertTrue(decoded.contains("NIT: 900123456-7"));
        assertTrue(decoded.contains("Calle 10 # 5-20"));
        assertTrue(decoded.contains("Bogota, Cundinamarca"));
        assertTrue(decoded.contains("Tel: 3001234567"));
    }

    @Test
    void includesConfiguredReceiptHeaderAndFooter() {
        String decoded = decode(formatTicket(sampleTicket(), PaperWidth.MM_58));

        assertTrue(decoded.contains("Gracias por su compra"));
        assertTrue(decoded.contains("No aplica devoluciones sin ticket"));
    }

    @Test
    void includesSaleNumberCashierAndCustomer() {
        String decoded = decode(formatTicket(sampleTicket(), PaperWidth.MM_58));

        assertTrue(decoded.contains("Venta POS-0001"));
        assertTrue(decoded.contains("Cajero: Maria Lopez"));
        assertTrue(decoded.contains("Cliente: Juan Perez"));
    }

    @Test
    void formatsTotalsWithColombianThousandsSeparator() {
        String decoded = decode(formatTicket(sampleTicket(), PaperWidth.MM_58));

        assertTrue(decoded.contains("$15.000"));
        assertTrue(decoded.contains("TOTAL"));
    }

    @Test
    void omitsDiscountLineWhenDiscountIsNull() {
        SaleData saleWithoutDiscount = new SaleData(
                "POS-0002",
                List.of(sampleItem()),
                List.of(samplePayment()),
                new BigDecimal("15000"),
                new BigDecimal("2850"),
                null,
                new BigDecimal("17850"),
                "Maria Lopez",
                null,
                LocalDateTime.of(2026, 8, 16, 10, 30));
        TicketData ticket = new TicketData(saleWithoutDiscount, sampleBranding());

        String decoded = decode(formatTicket(ticket, PaperWidth.MM_58));

        assertFalse(decoded.contains("Descuento"));
    }

    @Test
    void includesEachPaymentWithReferenceWhenPresent() {
        String decoded = decode(formatTicket(sampleTicket(), PaperWidth.MM_58));

        assertTrue(decoded.contains("EFECTIVO"));
    }

    @Test
    void truncatesProductNameLongerThanCharsPerLine() {
        SaleItemData longNameItem = new SaleItemData(
                "Producto con un nombre extremadamente largo que no cabe en el ticket termico",
                BigDecimal.ONE,
                new BigDecimal("15000"),
                new BigDecimal("15000"),
                new BigDecimal("19"),
                new BigDecimal("2850"),
                null);
        SaleData sale = new SaleData(
                "POS-0003",
                List.of(longNameItem),
                List.of(samplePayment()),
                new BigDecimal("15000"),
                new BigDecimal("2850"),
                null,
                new BigDecimal("17850"),
                "Maria Lopez",
                null,
                LocalDateTime.of(2026, 8, 16, 10, 30));
        TicketData ticket = new TicketData(sale, sampleBranding());

        String decoded = decode(formatTicket(ticket, PaperWidth.MM_58));

        assertTrue(decoded.contains(longNameItem.productName().substring(0, PaperWidth.MM_58.charsPerLine())));
        assertTrue(!decoded.contains(longNameItem.productName()));
    }

    @Test
    void mm80ProducesWiderSeparatorThanMm58() {
        String decoded58 = decode(formatTicket(sampleTicket(), PaperWidth.MM_58));
        String decoded80 = decode(formatTicket(sampleTicket(), PaperWidth.MM_80));

        String separator58 = "-".repeat(PaperWidth.MM_58.charsPerLine());
        String separator80 = "-".repeat(PaperWidth.MM_80.charsPerLine());
        assertTrue(decoded58.contains(separator58));
        assertTrue(decoded80.contains(separator80));
        assertTrue(!decoded58.contains(separator80));
    }

    private static TicketData sampleTicket() {
        SaleData sale = new SaleData(
                "POS-0001",
                List.of(sampleItem()),
                List.of(samplePayment()),
                new BigDecimal("15000"),
                new BigDecimal("2850"),
                null,
                new BigDecimal("17850"),
                "Maria Lopez",
                "Juan Perez",
                LocalDateTime.of(2026, 8, 16, 10, 30));
        return new TicketData(sale, sampleBranding());
    }

    private static SaleItemData sampleItem() {
        return new SaleItemData(
                "Gaseosa 400ml",
                BigDecimal.ONE,
                new BigDecimal("15000"),
                new BigDecimal("15000"),
                new BigDecimal("19"),
                new BigDecimal("2850"),
                null);
    }

    private static SalePaymentData samplePayment() {
        return new SalePaymentData("EFECTIVO", new BigDecimal("17850"), null);
    }

    private static BrandingData sampleBranding() {
        return new BrandingData(
                "Tienda Amara",
                "Amara",
                "900123456-7",
                "Calle 10 # 5-20",
                "Bogota",
                "Cundinamarca",
                "3001234567",
                "contacto@amara.com",
                "Gracias por su compra",
                "No aplica devoluciones sin ticket");
    }

    private byte[] formatTicket(TicketData ticket, PaperWidth width) {
        return formatter.format(ticket, width, NO_LOGO);
    }

    private static String decode(byte[] data) {
        return new String(data, EscPosCommands.CHARSET);
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
