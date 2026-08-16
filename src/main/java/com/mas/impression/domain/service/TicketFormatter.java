package com.mas.impression.domain.service;

import com.mas.impression.domain.model.BrandingData;
import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.SaleData;
import com.mas.impression.domain.model.SaleItemData;
import com.mas.impression.domain.model.SalePaymentData;
import com.mas.impression.domain.model.TicketData;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Lays out a {@link TicketData} as raw ESC/POS bytes for the given {@link PaperWidth}. Pure and
 * framework-free: no I/O, no recomputation of totals/taxes — those arrive already confirmed by
 * the backend and are only formatted here.
 */
public class TicketFormatter {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale COP_LOCALE = new Locale("es", "CO");

    public byte[] format(TicketData ticket, PaperWidth paperWidth, byte[] logoRaster) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeInit(out);
        writeHeader(out, ticket.branding(), paperWidth, logoRaster);
        writeItems(out, ticket.sale().items(), paperWidth);
        writeTotals(out, ticket.sale(), paperWidth);
        writePayments(out, ticket.sale().payments(), paperWidth);
        writeFooter(out, ticket, paperWidth);
        writeCut(out);
        return out.toByteArray();
    }

    private static void writeInit(ByteArrayOutputStream out) {
        out.writeBytes(EscPosCommands.INIT);
        out.writeBytes(EscPosCommands.SELECT_CODEPAGE_850);
    }

    private void writeHeader(ByteArrayOutputStream out, BrandingData branding, PaperWidth width, byte[] logoRaster) {
        out.writeBytes(EscPosCommands.ALIGN_CENTER);
        if (logoRaster != null && logoRaster.length > 0) {
            out.writeBytes(logoRaster);
            out.write(EscPosCommands.LINE_FEED);
        }
        out.writeBytes(EscPosCommands.BOLD_ON);
        if (isNotBlank(branding.businessName())) {
            writeLine(out, branding.businessName());
        }
        out.writeBytes(EscPosCommands.BOLD_OFF);
        if (isNotBlank(branding.tradeName())) {
            writeLine(out, branding.tradeName());
        }
        if (isNotBlank(branding.nit())) {
            writeLine(out, "NIT: " + branding.nit());
        }
        if (isNotBlank(branding.address())) {
            writeLine(out, branding.address());
        }
        String cityLine = joinNonBlank(branding.city(), branding.department());
        if (isNotBlank(cityLine)) {
            writeLine(out, cityLine);
        }
        if (isNotBlank(branding.phone())) {
            writeLine(out, "Tel: " + branding.phone());
        }
        out.writeBytes(EscPosCommands.ALIGN_LEFT);
        if (isNotBlank(branding.receiptHeader())) {
            writeSeparator(out, width);
            writeLine(out, branding.receiptHeader());
        }
        writeSeparator(out, width);
    }

    private void writeItems(ByteArrayOutputStream out, List<SaleItemData> items, PaperWidth width) {
        for (SaleItemData item : items) {
            writeItemLine(out, item, width);
        }
        writeSeparator(out, width);
    }

    private void writeItemLine(ByteArrayOutputStream out, SaleItemData item, PaperWidth width) {
        int chars = width.charsPerLine();
        writeLine(out, truncate(item.productName(), chars));
        String qtyPrice = formatQuantity(item.quantity()) + " x " + formatCurrency(item.unitPrice());
        writeLine(out, twoColumnLine(qtyPrice, formatCurrency(item.subtotal()), chars));
        if (item.discount() != null && item.discount().signum() > 0) {
            writeLine(out, twoColumnLine("  Descuento", "-" + formatCurrency(item.discount()), chars));
        }
    }

    private void writeTotals(ByteArrayOutputStream out, SaleData sale, PaperWidth width) {
        int chars = width.charsPerLine();
        writeLine(out, twoColumnLine("Subtotal", formatCurrency(sale.subtotal()), chars));
        writeLine(out, twoColumnLine("Impuestos", formatCurrency(sale.taxTotal()), chars));
        if (sale.discount() != null && sale.discount().signum() > 0) {
            writeLine(out, twoColumnLine("Descuento", "-" + formatCurrency(sale.discount()), chars));
        }
        out.writeBytes(EscPosCommands.BOLD_ON);
        writeLine(out, twoColumnLine("TOTAL", formatCurrency(sale.total()), chars));
        out.writeBytes(EscPosCommands.BOLD_OFF);
        writeSeparator(out, width);
    }

    private void writePayments(ByteArrayOutputStream out, List<SalePaymentData> payments, PaperWidth width) {
        for (SalePaymentData payment : payments) {
            writeLine(out, twoColumnLine(paymentLabel(payment), formatCurrency(payment.amount()), width.charsPerLine()));
        }
        if (!payments.isEmpty()) {
            writeSeparator(out, width);
        }
    }

    private static String paymentLabel(SalePaymentData payment) {
        return isNotBlank(payment.reference())
                ? payment.paymentMethod() + " (" + payment.reference() + ")"
                : payment.paymentMethod();
    }

    private void writeFooter(ByteArrayOutputStream out, TicketData ticket, PaperWidth width) {
        SaleData sale = ticket.sale();
        out.writeBytes(EscPosCommands.ALIGN_CENTER);
        writeLine(out, "Venta " + sale.saleNumber());
        if (sale.createdAt() != null) {
            writeLine(out, sale.createdAt().format(DATE_TIME_FORMAT));
        }
        if (isNotBlank(sale.cashierName())) {
            writeLine(out, "Cajero: " + sale.cashierName());
        }
        if (isNotBlank(sale.customerName())) {
            writeLine(out, "Cliente: " + sale.customerName());
        }
        if (isNotBlank(ticket.branding().receiptFooter())) {
            writeSeparator(out, width);
            writeLine(out, ticket.branding().receiptFooter());
        }
    }

    private static void writeCut(ByteArrayOutputStream out) {
        writeLine(out, "");
        writeLine(out, "");
        out.writeBytes(EscPosCommands.PARTIAL_CUT);
    }

    private static void writeSeparator(ByteArrayOutputStream out, PaperWidth width) {
        writeLine(out, "-".repeat(width.charsPerLine()));
    }

    private static void writeLine(ByteArrayOutputStream out, String text) {
        out.writeBytes(text.getBytes(EscPosCommands.CHARSET));
        out.write(EscPosCommands.LINE_FEED);
    }

    private static String truncate(String text, int width) {
        if (text == null) {
            return "";
        }
        return text.length() <= width ? text : text.substring(0, width);
    }

    private static String twoColumnLine(String left, String right, int width) {
        String safeLeft = left == null ? "" : left;
        String safeRight = right == null ? "" : right;
        int spaces = width - safeLeft.length() - safeRight.length();
        if (spaces < 1) {
            return truncate(safeLeft, Math.max(width - safeRight.length() - 1, 0)) + " " + safeRight;
        }
        return safeLeft + " ".repeat(spaces) + safeRight;
    }

    private static String formatCurrency(BigDecimal amount) {
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;
        NumberFormat format = NumberFormat.getIntegerInstance(COP_LOCALE);
        return "$" + format.format(safeAmount);
    }

    private static String formatQuantity(BigDecimal quantity) {
        BigDecimal safeQuantity = quantity == null ? BigDecimal.ZERO : quantity;
        return safeQuantity.stripTrailingZeros().toPlainString();
    }

    private static String joinNonBlank(String first, String second) {
        boolean hasFirst = isNotBlank(first);
        boolean hasSecond = isNotBlank(second);
        if (hasFirst && hasSecond) {
            return first + ", " + second;
        }
        return hasFirst ? first : (hasSecond ? second : "");
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
