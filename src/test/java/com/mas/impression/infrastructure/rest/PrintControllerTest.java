package com.mas.impression.infrastructure.rest;

import com.mas.impression.application.dto.PrintTicketRequest;
import com.mas.impression.application.dto.PrintTicketResponse;
import com.mas.impression.application.dto.PrinterStatusResponse;
import com.mas.impression.application.port.in.GetPrinterStatusUseCase;
import com.mas.impression.application.port.in.PrintTicketUseCase;
import com.mas.impression.domain.exception.PrinterNotConnectedException;
import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.PrintStatus;
import com.mas.impression.domain.model.PrinterConnectionType;
import com.mas.impression.infrastructure.config.AgentSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrintController.class)
@EnableConfigurationProperties(AgentSecurityProperties.class)
class PrintControllerTest {

    private static final String VALID_REQUEST_JSON = """
            {
              "sale": {
                "saleNumber": "POS-0001",
                "items": [{
                  "productName": "Gaseosa 400ml",
                  "quantity": 1,
                  "unitPrice": 15000,
                  "subtotal": 15000,
                  "taxRate": 19,
                  "taxAmount": 2850
                }],
                "payments": [{"paymentMethod": "EFECTIVO", "amount": 17850}],
                "subtotal": 15000,
                "taxTotal": 2850,
                "total": 17850,
                "cashierName": "Maria Lopez",
                "customerName": "Juan Perez",
                "createdAt": "2026-08-16T10:30:00"
              },
              "branding": {
                "businessName": "Tienda Amara",
                "nit": "900123456-7"
              }
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PrintTicketUseCase printTicketUseCase;

    @MockitoBean
    private GetPrinterStatusUseCase getPrinterStatusUseCase;

    @Test
    void printReturnsResponseFromUseCase() throws Exception {
        when(printTicketUseCase.printTicket(any(PrintTicketRequest.class))).thenReturn(
                new PrintTicketResponse("job-1", PrintStatus.PRINTED, "Ticket impreso correctamente.",
                        LocalDateTime.of(2026, 8, 16, 10, 30)));

        mockMvc.perform(post("/api/v1/print")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value("job-1"))
                .andExpect(jsonPath("$.status").value("PRINTED"));
    }

    @Test
    void printWithMissingRequiredFieldsReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/print")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void statusReturnsUseCaseResult() throws Exception {
        when(getPrinterStatusUseCase.getStatus()).thenReturn(
                new PrinterStatusResponse(true, PrinterConnectionType.NETWORK, PaperWidth.MM_58, null, null));

        mockMvc.perform(get("/api/v1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.transport").value("NETWORK"));
    }

    @Test
    void statusUseCaseFailureReturnsServiceUnavailable() throws Exception {
        when(getPrinterStatusUseCase.getStatus()).thenThrow(
                new PrinterNotConnectedException("La impresora no responde."));

        mockMvc.perform(get("/api/v1/status"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("PRINTER_NOT_CONNECTED"));
    }
}
