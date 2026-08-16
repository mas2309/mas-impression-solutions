package com.mas.impression.infrastructure.admin;

import com.mas.impression.application.dto.PrintTicketRequest;
import com.mas.impression.application.dto.PrintTicketResponse;
import com.mas.impression.application.dto.PrinterConfigRequest;
import com.mas.impression.application.port.in.ConfigurePrinterUseCase;
import com.mas.impression.application.port.in.GetPrintHistoryUseCase;
import com.mas.impression.application.port.in.GetPrinterStatusUseCase;
import com.mas.impression.application.port.in.PrintTicketUseCase;
import com.mas.impression.domain.model.PrintStatus;
import com.mas.impression.infrastructure.config.AgentSecurityProperties;
import com.mas.impression.infrastructure.printer.bluetooth.BluetoothPortGateway;
import com.mas.impression.infrastructure.printer.usb.UsbPrintServiceGateway;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Local, server-rendered admin UI — status, printer configuration, print history and a test
 * print. Never used by the POS frontend, only by whoever installs/supports the agent on this PC.
 */
@Controller
@RequestMapping("/admin")
public class AdminUiController {

    private static final int DASHBOARD_HISTORY_SIZE = 5;
    private static final int HISTORY_PAGE_SIZE = 50;

    private final GetPrinterStatusUseCase statusUseCase;
    private final ConfigurePrinterUseCase configureUseCase;
    private final GetPrintHistoryUseCase historyUseCase;
    private final PrintTicketUseCase printTicketUseCase;
    private final AgentSecurityProperties securityProperties;
    private final UsbPrintServiceGateway usbGateway;
    private final BluetoothPortGateway bluetoothGateway;

    public AdminUiController(GetPrinterStatusUseCase statusUseCase, ConfigurePrinterUseCase configureUseCase,
            GetPrintHistoryUseCase historyUseCase, PrintTicketUseCase printTicketUseCase,
            AgentSecurityProperties securityProperties, UsbPrintServiceGateway usbGateway,
            BluetoothPortGateway bluetoothGateway) {
        this.statusUseCase = statusUseCase;
        this.configureUseCase = configureUseCase;
        this.historyUseCase = historyUseCase;
        this.printTicketUseCase = printTicketUseCase;
        this.securityProperties = securityProperties;
        this.usbGateway = usbGateway;
        this.bluetoothGateway = bluetoothGateway;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("status", statusUseCase.getStatus());
        model.addAttribute("history", historyUseCase.getRecent(DASHBOARD_HISTORY_SIZE));
        model.addAttribute("agentToken", securityProperties.getToken());
        return "admin/dashboard";
    }

    @GetMapping("/config")
    public String configForm(Model model) {
        model.addAttribute("config", configureUseCase.getConfig());
        model.addAttribute("usbPrintServices", usbGateway.listPrintServiceNames());
        model.addAttribute("bluetoothPorts", bluetoothGateway.listPortNames());
        return "admin/config";
    }

    @PostMapping("/config")
    public String updateConfig(@Valid @ModelAttribute("request") PrinterConfigRequest request,
            BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Configuración inválida. Verifica los campos.");
            return "redirect:/admin/config";
        }
        configureUseCase.updateConfig(request);
        redirectAttributes.addFlashAttribute("success", "Configuración guardada.");
        return "redirect:/admin/config";
    }

    @GetMapping("/history")
    public String history(Model model) {
        model.addAttribute("history", historyUseCase.getRecent(HISTORY_PAGE_SIZE));
        return "admin/history";
    }

    @PostMapping("/test-print")
    public String testPrint(RedirectAttributes redirectAttributes) {
        PrintTicketResponse response = printTicketUseCase.printTicket(sampleRequest());
        String attribute = response.status() == PrintStatus.PRINTED ? "success" : "error";
        redirectAttributes.addFlashAttribute(attribute, response.message());
        return "redirect:/admin";
    }

    private static PrintTicketRequest sampleRequest() {
        return new PrintTicketRequest(sampleSale(), sampleBranding());
    }

    private static PrintTicketRequest.SalePayload sampleSale() {
        PrintTicketRequest.SaleItemPayload item = new PrintTicketRequest.SaleItemPayload(
                "Producto de prueba", BigDecimal.ONE, new BigDecimal("10000"), new BigDecimal("10000"),
                new BigDecimal("19"), new BigDecimal("1900"), null);
        PrintTicketRequest.SalePaymentPayload payment = new PrintTicketRequest.SalePaymentPayload(
                "EFECTIVO", new BigDecimal("11900"), null);
        return new PrintTicketRequest.SalePayload(
                null, "TEST-0001", List.of(item), List.of(payment), new BigDecimal("10000"),
                new BigDecimal("1900"), null, new BigDecimal("11900"), "Agente de impresión", null,
                LocalDateTime.now());
    }

    private static PrintTicketRequest.BrandingPayload sampleBranding() {
        return new PrintTicketRequest.BrandingPayload(
                "Comercio de prueba", null, "000000000-0", null, null, null, null, null,
                null, "Este es un ticket de prueba generado desde el panel de administración.");
    }
}
