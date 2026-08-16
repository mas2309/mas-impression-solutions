package com.mas.impression.infrastructure.printer.network;

import com.mas.impression.domain.exception.PrinterNotConnectedException;
import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.model.PrinterConnectionType;
import com.mas.impression.domain.model.PrinterState;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkPrinterAdapterTest {

    @Test
    void connectAndWriteSendsBytesToServer() throws IOException, InterruptedException {
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            CompletableFuture<byte[]> received = CompletableFuture.supplyAsync(() -> acceptAndRead(serverSocket));

            NetworkPrinterAdapter adapter = new NetworkPrinterAdapter();
            adapter.connect(configFor(serverSocket.getLocalPort()));
            adapter.write("hola".getBytes(StandardCharsets.UTF_8));
            PrinterState connectedState = adapter.status();
            adapter.disconnect();

            byte[] receivedBytes = received.orTimeout(2, TimeUnit.SECONDS).join();
            assertArrayEquals("hola".getBytes(StandardCharsets.UTF_8), receivedBytes);
            assertTrue(connectedState.connected());
            assertNotNull(connectedState.lastPrintAt());
        }
    }

    @Test
    void connectToUnreachablePortThrowsPrinterNotConnectedException() throws IOException {
        int freePort = allocateThenReleasePort();
        NetworkPrinterAdapter adapter = new NetworkPrinterAdapter();

        assertThrows(PrinterNotConnectedException.class, () -> adapter.connect(configFor(freePort)));
        assertFalse(adapter.status().connected());
    }

    @Test
    void writeWithoutConnectingThrowsPrinterNotConnectedException() {
        NetworkPrinterAdapter adapter = new NetworkPrinterAdapter();

        assertThrows(PrinterNotConnectedException.class, () -> adapter.write(new byte[] {1}));
    }

    @Test
    void disconnectWithoutConnectingIsSafe() {
        NetworkPrinterAdapter adapter = new NetworkPrinterAdapter();

        assertDoesNotThrow(adapter::disconnect);
    }

    @Test
    void initialStatusIsNotConnected() {
        NetworkPrinterAdapter adapter = new NetworkPrinterAdapter();

        assertFalse(adapter.status().connected());
    }

    private static byte[] acceptAndRead(ServerSocket serverSocket) {
        try (Socket client = serverSocket.accept()) {
            return client.getInputStream().readAllBytes();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static int allocateThenReleasePort() throws IOException {
        try (ServerSocket temporary = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            return temporary.getLocalPort();
        }
    }

    private static PrinterConnectionConfig configFor(int port) {
        return new PrinterConnectionConfig(
                PrinterConnectionType.NETWORK, PaperWidth.MM_58, "127.0.0.1", port, 1000, null, null);
    }
}
