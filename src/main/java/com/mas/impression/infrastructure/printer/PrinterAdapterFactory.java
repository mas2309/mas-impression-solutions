package com.mas.impression.infrastructure.printer;

import com.mas.impression.domain.model.PrinterConnectionType;
import com.mas.impression.domain.port.PrinterPort;
import com.mas.impression.domain.port.PrinterPortResolver;
import com.mas.impression.infrastructure.printer.bluetooth.BluetoothPrinterAdapter;
import com.mas.impression.infrastructure.printer.network.NetworkPrinterAdapter;
import com.mas.impression.infrastructure.printer.usb.UsbPrinterAdapter;
import org.springframework.stereotype.Component;

@Component
public class PrinterAdapterFactory implements PrinterPortResolver {

    private final NetworkPrinterAdapter networkAdapter;
    private final UsbPrinterAdapter usbAdapter;
    private final BluetoothPrinterAdapter bluetoothAdapter;

    public PrinterAdapterFactory(NetworkPrinterAdapter networkAdapter, UsbPrinterAdapter usbAdapter,
            BluetoothPrinterAdapter bluetoothAdapter) {
        this.networkAdapter = networkAdapter;
        this.usbAdapter = usbAdapter;
        this.bluetoothAdapter = bluetoothAdapter;
    }

    @Override
    public PrinterPort resolve(PrinterConnectionType connectionType) {
        return switch (connectionType) {
            case NETWORK -> networkAdapter;
            case USB -> usbAdapter;
            case BLUETOOTH -> bluetoothAdapter;
        };
    }
}
