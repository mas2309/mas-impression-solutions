package com.mas.impression.infrastructure.rest;

import com.mas.impression.application.dto.DeviceInfoResponse;
import com.mas.impression.infrastructure.config.AgentDeviceIdInitializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Lets the frontend read this install's device identity before pairing it with the ERP backend
 * (see {@code PrintAgentDeviceController} there) — never used for printing itself. Falls under
 * {@code /api/**}, so {@link com.mas.impression.infrastructure.config.AgentTokenAuthFilter}
 * already guards it with the same pairing token as {@code /print} and {@code /status}.
 */
@RestController
@RequestMapping("/api/v1")
public class DeviceController {

    private final AgentDeviceIdInitializer deviceIdInitializer;

    public DeviceController(AgentDeviceIdInitializer deviceIdInitializer) {
        this.deviceIdInitializer = deviceIdInitializer;
    }

    @GetMapping("/device")
    public DeviceInfoResponse device() {
        return new DeviceInfoResponse(deviceIdInitializer.getDeviceId(), resolveHostname());
    }

    private static String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return null;
        }
    }
}
