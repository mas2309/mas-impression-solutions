package com.mas.impression.application.service;

import com.mas.impression.application.dto.PrintTicketRequest;
import com.mas.impression.application.dto.PrintTicketResponse;
import com.mas.impression.application.mapper.TicketDataMapper;
import com.mas.impression.application.port.in.PrintTicketUseCase;
import com.mas.impression.domain.exception.PrinterNotConnectedException;
import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.PrintJob;
import com.mas.impression.domain.model.PrintStatus;
import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.model.TicketData;
import com.mas.impression.domain.port.PrinterPort;
import com.mas.impression.domain.port.PrinterPortResolver;
import com.mas.impression.domain.port.PrintJobHistoryPort;
import com.mas.impression.domain.service.TicketFormatter;
import com.mas.impression.infrastructure.asset.LogoAssetProvider;
import com.mas.impression.infrastructure.asset.RemoteLogoProvider;
import com.mas.impression.infrastructure.notification.BackendNotifierClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Orchestrates a single print attempt: format the ticket, send it to whichever {@link PrinterPort}
 * transport the current config resolves to, and report the outcome. A failed print is a normal
 * operational outcome here, not a server error, so it is returned as a {@link PrintStatus#FAILED}
 * response rather than thrown.
 */
@Service
public class PrintTicketService implements PrintTicketUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(PrintTicketService.class);

    private final PrinterPortResolver adapterResolver;
    private final TicketFormatter ticketFormatter;
    private final EffectivePrinterConfigResolver configResolver;
    private final PrintJobHistoryPort history;
    private final LogoAssetProvider logoAssetProvider;
    private final RemoteLogoProvider remoteLogoProvider;
    private final BackendNotifierClient backendNotifier;

    public PrintTicketService(PrinterPortResolver adapterResolver, TicketFormatter ticketFormatter,
            EffectivePrinterConfigResolver configResolver, PrintJobHistoryPort history,
            LogoAssetProvider logoAssetProvider, RemoteLogoProvider remoteLogoProvider,
            BackendNotifierClient backendNotifier) {
        this.adapterResolver = adapterResolver;
        this.ticketFormatter = ticketFormatter;
        this.configResolver = configResolver;
        this.history = history;
        this.logoAssetProvider = logoAssetProvider;
        this.remoteLogoProvider = remoteLogoProvider;
        this.backendNotifier = backendNotifier;
    }

    @Override
    public PrintTicketResponse printTicket(PrintTicketRequest request) {
        String jobId = UUID.randomUUID().toString();
        String saleNumber = request.sale().saleNumber();
        LocalDateTime attemptedAt = LocalDateTime.now();
        PrinterConnectionConfig config = configResolver.resolve();
        PrinterPort printerPort = adapterResolver.resolve(config.connectionType());
        TicketData ticketData = TicketDataMapper.toDomain(request);
        byte[] logoRaster = resolveLogoRaster(request.branding().logoUrl(), config.paperWidth());
        byte[] payload = ticketFormatter.format(ticketData, config.paperWidth(), logoRaster);

        PrintTicketResponse response;
        try {
            printerPort.connect(config);
            printerPort.write(payload);
            LocalDateTime printedAt = LocalDateTime.now();
            response = new PrintTicketResponse(jobId, PrintStatus.PRINTED, "Ticket impreso correctamente.",
                    printedAt);
            backendNotifier.notifyPrinted(request.sale().saleId(), printedAt);
        } catch (PrinterNotConnectedException ex) {
            LOG.warn("No se pudo imprimir la venta {}: {}", saleNumber, ex.getMessage());
            response = new PrintTicketResponse(jobId, PrintStatus.FAILED, ex.getMessage(), null);
        } finally {
            printerPort.disconnect();
        }

        history.record(new PrintJob(
                jobId, saleNumber, response.status(), response.message(), attemptedAt, response.printedAt()));
        return response;
    }

    /**
     * Prefers the business's own logo (sent per request); falls back to the bundled default
     * when there's none or it couldn't be fetched — this keeps existing callers that never send
     * {@code logoUrl} (e.g. Amará) printing exactly as before.
     */
    private byte[] resolveLogoRaster(String logoUrl, PaperWidth paperWidth) {
        byte[] tenantLogo = remoteLogoProvider.rasterFor(logoUrl, paperWidth);
        return tenantLogo != null ? tenantLogo : logoAssetProvider.rasterFor(paperWidth);
    }
}
