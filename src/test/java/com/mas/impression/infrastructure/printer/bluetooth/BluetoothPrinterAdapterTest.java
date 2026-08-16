package com.mas.impression.infrastructure.printer.bluetooth;

import com.mas.impression.domain.exception.PrinterNotConnectedException;
import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.model.PrinterConnectionType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BluetoothPrinterAdapterTest {

    private final FakeGateway gateway = new FakeGateway();
    private final BluetoothPrinterAdapter adapter = new BluetoothPrinterAdapter(gateway);

    @Test
    void connectSucceedsWhenPortOpens() {
        adapter.connect(configFor("COM5"));

        assertTrue(adapter.status().connected());
    }

    @Test
    void connectFailsWhenNoPortConfigured() {
        assertThrows(PrinterNotConnectedException.class, () -> adapter.connect(configFor(null)));
    }

    @Test
    void connectFailsWhenOpenThrows() {
        gateway.failOnOpen = true;

        assertThrows(PrinterNotConnectedException.class, () -> adapter.connect(configFor("COM5")));
        assertFalse(adapter.status().connected());
    }

    @Test
    void writeWithoutConnectingThrows() {
        assertThrows(PrinterNotConnectedException.class, () -> adapter.write(new byte[] {1}));
    }

    @Test
    void writeSendsDataAfterConnect() {
        adapter.connect(configFor("COM5"));

        adapter.write("hola".getBytes(StandardCharsets.UTF_8));

        assertArrayEquals("hola".getBytes(StandardCharsets.UTF_8), gateway.lastHandle.written);
        assertNotNull(adapter.status().lastPrintAt());
    }

    @Test
    void writeFailurePropagatesAsPrinterNotConnected() {
        adapter.connect(configFor("COM5"));
        gateway.lastHandle.failOnWrite = true;

        assertThrows(PrinterNotConnectedException.class, () -> adapter.write(new byte[] {1}));
    }

    @Test
    void disconnectClosesTheHandle() {
        adapter.connect(configFor("COM5"));

        adapter.disconnect();

        assertTrue(gateway.lastHandle.closed);
        assertThrows(PrinterNotConnectedException.class, () -> adapter.write(new byte[] {1}));
    }

    @Test
    void disconnectWithoutConnectingIsSafe() {
        adapter.disconnect();
    }

    private static PrinterConnectionConfig configFor(String portName) {
        return new PrinterConnectionConfig(PrinterConnectionType.BLUETOOTH, PaperWidth.MM_58, null, 0, 0, null, portName);
    }

    private static final class FakeGateway implements BluetoothPortGateway {

        private boolean failOnOpen;
        private FakeHandle lastHandle;

        @Override
        public List<String> listPortNames() {
            return List.of("COM5");
        }

        @Override
        public BluetoothPortHandle open(String portName) throws IOException {
            if (failOnOpen) {
                throw new IOException("Fallo simulado al abrir el puerto.");
            }
            lastHandle = new FakeHandle();
            return lastHandle;
        }
    }

    private static final class FakeHandle implements BluetoothPortHandle {

        private boolean failOnWrite;
        private boolean closed;
        private byte[] written;

        @Override
        public void write(byte[] data) throws IOException {
            if (failOnWrite) {
                throw new IOException("Fallo simulado al escribir.");
            }
            written = data;
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
