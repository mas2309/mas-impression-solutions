package com.mas.impression.application.dto;

import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.PrinterConnectionType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Admin-editable printer configuration, submitted from the {@code /admin/config} form. The form
 * always submits every field, but only the ones relevant to {@code connectionType} are validated
 * as required — the {@code isXxxWhenYyy} methods below are per-transport cross-field checks.
 */
public record PrinterConfigRequest(
        @NotNull PrinterConnectionType connectionType,
        @NotNull PaperWidth paperWidth,
        String host,
        @Min(1) @Max(65535) int port,
        @Min(100) int connectTimeoutMillis,
        String usbPrintServiceName,
        String bluetoothPortName) {

    @AssertTrue(message = "El host es obligatorio cuando el transporte es de red.")
    public boolean isHostPresentWhenNetwork() {
        return connectionType != PrinterConnectionType.NETWORK || isNotBlank(host);
    }

    @AssertTrue(message = "Selecciona una impresora USB.")
    public boolean isUsbPrintServicePresentWhenUsb() {
        return connectionType != PrinterConnectionType.USB || isNotBlank(usbPrintServiceName);
    }

    @AssertTrue(message = "Selecciona un puerto Bluetooth.")
    public boolean isBluetoothPortPresentWhenBluetooth() {
        return connectionType != PrinterConnectionType.BLUETOOTH || isNotBlank(bluetoothPortName);
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
