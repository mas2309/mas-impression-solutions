package com.mas.impression.infrastructure.rest;

import com.mas.impression.infrastructure.config.AgentDeviceIdInitializer;
import com.mas.impression.infrastructure.config.AgentSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
@EnableConfigurationProperties(AgentSecurityProperties.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgentDeviceIdInitializer deviceIdInitializer;

    @Test
    void deviceReturnsThePersistedDeviceId() throws Exception {
        when(deviceIdInitializer.getDeviceId()).thenReturn("fixed-device-id");

        mockMvc.perform(get("/api/v1/device"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("fixed-device-id"));
    }
}
