package com.mas.impression.infrastructure.printer.usb;

import com.mas.impression.domain.exception.PrinterNotConnectedException;
import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.model.PrinterConnectionType;
import org.junit.jupiter.api.Test;

import javax.print.PrintException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsbPrinterAdapterTest {

    private final FakeGateway gateway = new FakeGateway();
    private final UsbPrinterAdapter adapter = new UsbPrinterAdapter(gateway);

    @Test
    void connectSucceedsWhenServiceExists() {
        gateway.existingServices.add("POS-58");

        adapter.connect(configFor("POS-58"));

        assertTrue(adapter.status().connected());
    }

    @Test
    void connectFailsWhenServiceMissing() {
        assertThrows(PrinterNotConnectedException.class, () -> adapter.connect(configFor("Desconocida")));
        assertFalse(adapter.status().connected());
    }

    @Test
    void connectFailsWhenNoServiceConfigured() {
        assertThrows(PrinterNotConnectedException.class, () -> adapter.connect(configFor(null)));
    }

    @Test
    void writeWithoutConnectingThrows() {
        assertThrows(PrinterNotConnectedException.class, () -> adapter.write(new byte[] {1}));
    }

    @Test
    void writeSendsDataAfterConnect() {
        gateway.existingServices.add("POS-58");
        adapter.connect(configFor("POS-58"));

        adapter.write("hola".getBytes(StandardCharsets.UTF_8));

        assertArrayEquals("hola".getBytes(StandardCharsets.UTF_8), gateway.lastPrinted);
        assertNotNull(adapter.status().lastPrintAt());
    }

    @Test
    void writeFailurePropagatesAsPrinterNotConnected() {
        gateway.existingServices.add("POS-58");
        gateway.failOnPrint = true;
        adapter.connect(configFor("POS-58"));

        assertThrows(PrinterNotConnectedException.class, () -> adapter.write(new byte[] {1}));
    }

    @Test
    void disconnectClearsActiveService() {
        gateway.existingServices.add("POS-58");
        adapter.connect(configFor("POS-58"));

        adapter.disconnect();

        assertThrows(PrinterNotConnectedException.class, () -> adapter.write(new byte[] {1}));
    }

    private static PrinterConnectionConfig configFor(String serviceName) {
        return new PrinterConnectionConfig(
                PrinterConnectionType.USB, PaperWidth.MM_58, null, 0, 0, serviceName, null);
    }

    private static final class FakeGateway implements UsbPrintServiceGateway {

        private final Set<String> existingServices = new HashSet<>();
        private byte[] lastPrinted;
        private boolean failOnPrint;

        @Override
        public List<String> listPrintServiceNames() {
            return List.copyOf(existingServices);
        }

        @Override
        public boolean printServiceExists(String printServiceName) {
            return existingServices.contains(printServiceName);
        }

        @Override
        public void print(String printServiceName, byte[] data) throws PrintException {
            if (failOnPrint) {
                throw new PrintException("Fallo simulado de impresión.");
            }
            lastPrinted = data;
        }
    }
}
