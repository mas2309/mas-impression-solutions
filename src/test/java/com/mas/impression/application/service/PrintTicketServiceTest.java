package com.mas.impression.application.service;

import com.mas.impression.application.dto.PrintTicketRequest;
import com.mas.impression.application.dto.PrintTicketResponse;
import com.mas.impression.domain.exception.PrinterNotConnectedException;
import com.mas.impression.domain.model.PrintJob;
import com.mas.impression.domain.model.PrintStatus;
import com.mas.impression.domain.port.PrinterConfigRepositoryPort;
import com.mas.impression.domain.port.PrinterPort;
import com.mas.impression.domain.port.PrinterPortResolver;
import com.mas.impression.domain.port.PrintJobHistoryPort;
import com.mas.impression.domain.service.TicketFormatter;
import com.mas.impression.infrastructure.asset.LogoAssetProvider;
import com.mas.impression.infrastructure.asset.RemoteLogoProvider;
import com.mas.impression.infrastructure.config.PrinterProperties;
import com.mas.impression.infrastructure.notification.BackendNotifierClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrintTicketServiceTest {

    private final PrinterPort printerPort = mock(PrinterPort.class);
    private final TicketFormatter ticketFormatter = mock(TicketFormatter.class);
    private final PrintJobHistoryPort history = mock(PrintJobHistoryPort.class);
    private final LogoAssetProvider logoAssetProvider = mock(LogoAssetProvider.class);
    private final RemoteLogoProvider remoteLogoProvider = mock(RemoteLogoProvider.class);
    private final BackendNotifierClient backendNotifier = mock(BackendNotifierClient.class);
    private PrintTicketService service;

    @BeforeEach
    void setUp() {
        PrinterConfigRepositoryPort repository = mock(PrinterConfigRepositoryPort.class);
        when(repository.load()).thenReturn(Optional.empty());
        EffectivePrinterConfigResolver configResolver =
                new EffectivePrinterConfigResolver(repository, new PrinterProperties());
        PrinterPortResolver adapterResolver = mock(PrinterPortResolver.class);
        when(adapterResolver.resolve(any())).thenReturn(printerPort);
        service = new PrintTicketService(adapterResolver, ticketFormatter, configResolver, history,
                logoAssetProvider, remoteLogoProvider, backendNotifier);
        when(ticketFormatter.format(any(), any(), any())).thenReturn(new byte[] {1, 2, 3});
        when(logoAssetProvider.rasterFor(any())).thenReturn(new byte[0]);
        when(remoteLogoProvider.rasterFor(any(), any())).thenReturn(null);
    }

    @Test
    void printTicketReturnsPrintedWhenPrinterAcceptsTheJob() {
        PrintTicketResponse response = service.printTicket(validRequest());

        assertEquals(PrintStatus.PRINTED, response.status());
        assertNotNull(response.printedAt());
        assertNotNull(response.jobId());
        verify(printerPort).disconnect();
        verify(history).record(any(PrintJob.class));
        verify(backendNotifier).notifyPrinted(any(), any());
    }

    @Test
    void printTicketDoesNotNotifyBackendWhenPrintFails() {
        doThrow(new PrinterNotConnectedException("La impresora no responde."))
                .when(printerPort).connect(any());

        service.printTicket(validRequest());

        verify(backendNotifier, never()).notifyPrinted(any(), any());
    }

    @Test
    void printTicketReturnsFailedWhenConnectFails() {
        doThrow(new PrinterNotConnectedException("La impresora no responde."))
                .when(printerPort).connect(any());

        PrintTicketResponse response = service.printTicket(validRequest());

        assertEquals(PrintStatus.FAILED, response.status());
        assertEquals("La impresora no responde.", response.message());
        assertNull(response.printedAt());
        verify(printerPort).disconnect();
    }

    @Test
    void printTicketReturnsFailedWhenWriteFails() {
        doThrow(new PrinterNotConnectedException("Error al enviar datos a la impresora."))
                .when(printerPort).write(any());

        PrintTicketResponse response = service.printTicket(validRequest());

        assertEquals(PrintStatus.FAILED, response.status());
        verify(printerPort).disconnect();
    }

    @Test
    void printTicketFallsBackToBundledLogoWhenTenantHasNoLogoUrl() {
        service.printTicket(validRequest());

        verify(logoAssetProvider).rasterFor(any());
    }

    @Test
    void printTicketPrefersTenantLogoOverBundledDefault() {
        byte[] tenantLogo = {9, 9, 9};
        when(remoteLogoProvider.rasterFor(eq("https://cdn.example.com/logo.png"), any())).thenReturn(tenantLogo);

        PrintTicketRequest request = withLogoUrl("https://cdn.example.com/logo.png");
        service.printTicket(request);

        verify(ticketFormatter).format(any(), any(), eq(tenantLogo));
        verify(logoAssetProvider, never()).rasterFor(any());
    }

    private static PrintTicketRequest withLogoUrl(String logoUrl) {
        PrintTicketRequest base = validRequest();
        PrintTicketRequest.BrandingPayload branding = base.branding();
        PrintTicketRequest.BrandingPayload withLogo = new PrintTicketRequest.BrandingPayload(
                branding.businessName(), logoUrl, branding.tradeName(), branding.nit(), branding.address(),
                branding.city(), branding.department(), branding.phone(), branding.email(),
                branding.receiptHeader(), branding.receiptFooter());
        return new PrintTicketRequest(base.sale(), withLogo);
    }

    private static PrintTicketRequest validRequest() {
        PrintTicketRequest.SaleItemPayload item = new PrintTicketRequest.SaleItemPayload(
                "Gaseosa 400ml", BigDecimal.ONE, new BigDecimal("15000"), new BigDecimal("15000"),
                new BigDecimal("19"), new BigDecimal("2850"), null);
        PrintTicketRequest.SalePaymentPayload payment = new PrintTicketRequest.SalePaymentPayload(
                "EFECTIVO", new BigDecimal("17850"), null);
        PrintTicketRequest.SalePayload sale = new PrintTicketRequest.SalePayload(
                42L, "POS-0001", List.of(item), List.of(payment), new BigDecimal("15000"),
                new BigDecimal("2850"), null, new BigDecimal("17850"), "Maria Lopez", "Juan Perez",
                LocalDateTime.of(2026, 8, 16, 10, 30));
        PrintTicketRequest.BrandingPayload branding = new PrintTicketRequest.BrandingPayload(
                "Tienda Amara", null, "Amara", "900123456-7", "Calle 10 # 5-20", "Bogota", "Cundinamarca",
                "3001234567", "contacto@amara.com", "Gracias por su compra", "No aplica devoluciones");
        return new PrintTicketRequest(sale, branding);
    }
}
