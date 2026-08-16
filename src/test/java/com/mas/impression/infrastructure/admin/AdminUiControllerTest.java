package com.mas.impression.infrastructure.admin;

import com.mas.impression.application.dto.PrintTicketResponse;
import com.mas.impression.application.dto.PrinterConfigResponse;
import com.mas.impression.application.dto.PrinterStatusResponse;
import com.mas.impression.application.port.in.ConfigurePrinterUseCase;
import com.mas.impression.application.port.in.GetPrintHistoryUseCase;
import com.mas.impression.application.port.in.GetPrinterStatusUseCase;
import com.mas.impression.application.port.in.PrintTicketUseCase;
import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.PrintStatus;
import com.mas.impression.domain.model.PrinterConnectionType;
import com.mas.impression.infrastructure.config.AgentSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminUiController.class)
@EnableConfigurationProperties(AgentSecurityProperties.class)
class AdminUiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetPrinterStatusUseCase statusUseCase;

    @MockitoBean
    private ConfigurePrinterUseCase configureUseCase;

    @MockitoBean
    private GetPrintHistoryUseCase historyUseCase;

    @MockitoBean
    private PrintTicketUseCase printTicketUseCase;

    @Test
    void dashboardRendersStatusAndHistory() throws Exception {
        when(statusUseCase.getStatus()).thenReturn(
                new PrinterStatusResponse(true, PrinterConnectionType.NETWORK, PaperWidth.MM_58, null, null));
        when(historyUseCase.getRecent(5)).thenReturn(List.of());

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("status", "history", "agentToken"));
    }

    @Test
    void configFormRendersCurrentConfig() throws Exception {
        when(configureUseCase.getConfig()).thenReturn(
                new PrinterConfigResponse(PrinterConnectionType.NETWORK, PaperWidth.MM_58, "127.0.0.1", 9100, 3000));

        mockMvc.perform(get("/admin/config"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/config"))
                .andExpect(model().attributeExists("config"));
    }

    @Test
    void updateConfigWithValidDataSavesAndRedirects() throws Exception {
        when(configureUseCase.updateConfig(any())).thenReturn(
                new PrinterConfigResponse(PrinterConnectionType.NETWORK, PaperWidth.MM_80, "192.168.1.50", 9100, 3000));

        mockMvc.perform(post("/admin/config")
                        .param("connectionType", "NETWORK")
                        .param("paperWidth", "MM_80")
                        .param("host", "192.168.1.50")
                        .param("port", "9100")
                        .param("connectTimeoutMillis", "3000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/config"))
                .andExpect(flash().attributeExists("success"));

        verify(configureUseCase).updateConfig(any());
    }

    @Test
    void updateConfigWithBlankHostDoesNotSave() throws Exception {
        mockMvc.perform(post("/admin/config")
                        .param("connectionType", "NETWORK")
                        .param("paperWidth", "MM_58")
                        .param("host", "")
                        .param("port", "9100")
                        .param("connectTimeoutMillis", "3000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        verify(configureUseCase, never()).updateConfig(any());
    }

    @Test
    void historyRendersRecentJobs() throws Exception {
        when(historyUseCase.getRecent(50)).thenReturn(List.of());

        mockMvc.perform(get("/admin/history"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/history"))
                .andExpect(model().attributeExists("history"));
    }

    @Test
    void testPrintTriggersAndRedirectsWithSuccessFlash() throws Exception {
        when(printTicketUseCase.printTicket(any())).thenReturn(
                new PrintTicketResponse("job-1", PrintStatus.PRINTED, "Ticket impreso correctamente.",
                        LocalDateTime.now()));

        mockMvc.perform(post("/admin/test-print"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void testPrintFailureRedirectsWithErrorFlash() throws Exception {
        when(printTicketUseCase.printTicket(any())).thenReturn(
                new PrintTicketResponse("job-1", PrintStatus.FAILED, "La impresora no responde.", null));

        mockMvc.perform(post("/admin/test-print"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
    }
}
