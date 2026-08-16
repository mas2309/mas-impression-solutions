package com.mas.impression.infrastructure.rest;

import com.mas.impression.application.dto.PrintTicketRequest;
import com.mas.impression.application.dto.PrintTicketResponse;
import com.mas.impression.application.dto.PrinterStatusResponse;
import com.mas.impression.application.port.in.GetPrinterStatusUseCase;
import com.mas.impression.application.port.in.PrintTicketUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The only HTTP surface the POS frontend talks to, always over {@code http://localhost:PORT}.
 */
@RestController
@RequestMapping("/api/v1")
public class PrintController {

    private final PrintTicketUseCase printTicketUseCase;
    private final GetPrinterStatusUseCase getPrinterStatusUseCase;

    public PrintController(PrintTicketUseCase printTicketUseCase, GetPrinterStatusUseCase getPrinterStatusUseCase) {
        this.printTicketUseCase = printTicketUseCase;
        this.getPrinterStatusUseCase = getPrinterStatusUseCase;
    }

    @PostMapping("/print")
    public PrintTicketResponse print(@Valid @RequestBody PrintTicketRequest request) {
        return printTicketUseCase.printTicket(request);
    }

    @GetMapping("/status")
    public PrinterStatusResponse status() {
        return getPrinterStatusUseCase.getStatus();
    }
}
