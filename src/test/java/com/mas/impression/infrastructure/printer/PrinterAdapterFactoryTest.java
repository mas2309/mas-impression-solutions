package com.mas.impression.infrastructure.printer;

import com.mas.impression.domain.model.PrinterConnectionType;
import com.mas.impression.infrastructure.printer.bluetooth.BluetoothPrinterAdapter;
import com.mas.impression.infrastructure.printer.network.NetworkPrinterAdapter;
import com.mas.impression.infrastructure.printer.usb.UsbPrinterAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class PrinterAdapterFactoryTest {

    private final NetworkPrinterAdapter networkAdapter = mock(NetworkPrinterAdapter.class);
    private final UsbPrinterAdapter usbAdapter = mock(UsbPrinterAdapter.class);
    private final BluetoothPrinterAdapter bluetoothAdapter = mock(BluetoothPrinterAdapter.class);
    private final PrinterAdapterFactory factory =
            new PrinterAdapterFactory(networkAdapter, usbAdapter, bluetoothAdapter);

    @Test
    void resolvesNetworkAdapter() {
        assertSame(networkAdapter, factory.resolve(PrinterConnectionType.NETWORK));
    }

    @Test
    void resolvesUsbAdapter() {
        assertSame(usbAdapter, factory.resolve(PrinterConnectionType.USB));
    }

    @Test
    void resolvesBluetoothAdapter() {
        assertSame(bluetoothAdapter, factory.resolve(PrinterConnectionType.BLUETOOTH));
    }
}
