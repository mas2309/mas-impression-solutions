package com.mas.impression.infrastructure.notification;

import com.mas.impression.infrastructure.config.BackendProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BackendNotifierClientTest {

    @Test
    void doesNothingWhenBaseUrlIsBlank() {
        BackendNotifierClient client = new BackendNotifierClient(new BackendProperties());

        assertDoesNotThrow(() -> client.notifyPrinted(1L, LocalDateTime.now()));
    }

    @Test
    void doesNothingWhenSaleIdIsNull() {
        BackendProperties properties = new BackendProperties();
        properties.setBaseUrl("http://127.0.0.1:1");
        BackendNotifierClient client = new BackendNotifierClient(properties);

        assertDoesNotThrow(() -> client.notifyPrinted(null, LocalDateTime.now()));
    }

    @Test
    void swallowsFailureWhenBackendIsUnreachable() throws IOException {
        BackendProperties properties = new BackendProperties();
        properties.setBaseUrl("http://127.0.0.1:" + allocateThenReleasePort());
        properties.setConnectTimeoutMillis(500);
        BackendNotifierClient client = new BackendNotifierClient(properties);

        assertDoesNotThrow(() -> client.notifyPrinted(1L, LocalDateTime.now()));
    }

    @Test
    void sendsPutRequestToPrintStatusEndpointWhenConfigured() throws IOException {
        AtomicReference<String> receivedMethod = new AtomicReference<>();
        AtomicReference<String> receivedPath = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/api/v1/sales/42/print-status", exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            receivedPath.set(exchange.getRequestURI().getPath());
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();
        try {
            BackendProperties properties = new BackendProperties();
            properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
            BackendNotifierClient client = new BackendNotifierClient(properties);

            client.notifyPrinted(42L, LocalDateTime.now());

            assertEquals("PUT", receivedMethod.get());
            assertEquals("/api/v1/sales/42/print-status", receivedPath.get());
        } finally {
            server.stop(0);
        }
    }

    private static int allocateThenReleasePort() throws IOException {
        try (ServerSocket temporary = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            return temporary.getLocalPort();
        }
    }
}
