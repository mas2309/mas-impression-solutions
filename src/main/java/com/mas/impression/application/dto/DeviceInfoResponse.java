package com.mas.impression.application.dto;

/**
 * This install's device identity, read by the frontend before pairing with the ERP backend
 * (see {@code PrintAgentDeviceController} there) — never used for printing itself.
 */
public record DeviceInfoResponse(String deviceId, String hostname) {
}
