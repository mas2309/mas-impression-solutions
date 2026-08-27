package com.mas.impression.infrastructure.asset;

import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.service.LogoRasterConverter;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoteLogoProviderTest {

    private final RemoteLogoProvider provider = new RemoteLogoProvider(new LogoRasterConverter());
    private HttpServer server;
    private AtomicInteger requestCount;
    private byte[] logoBytes;

    @BeforeEach
    void setUp() throws IOException {
        logoBytes = readBundledLogoBytes();
        requestCount = new AtomicInteger();
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/logo.jpeg", exchange -> {
            requestCount.incrementAndGet();
            exchange.sendResponseHeaders(200, logoBytes.length);
            exchange.getResponseBody().write(logoBytes);
            exchange.close();
        });
        server.createContext("/missing.jpeg", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void returnsNullWhenUrlIsBlank() {
        assertNull(provider.rasterFor("", PaperWidth.MM_58));
        assertNull(provider.rasterFor(null, PaperWidth.MM_58));
    }

    @Test
    void returnsNullForAMalformedUrl() {
        assertNull(provider.rasterFor("not a url", PaperWidth.MM_58));
    }

    @Test
    void returnsNullWhenTheServerRespondsNotFound() {
        assertNull(provider.rasterFor(url("/missing.jpeg"), PaperWidth.MM_58));
    }

    @Test
    void fetchesAndRasterizesTheTenantLogo() {
        byte[] raster = provider.rasterFor(url("/logo.jpeg"), PaperWidth.MM_58);

        assertTrue(raster.length > 0);
        assertTrue(requestCount.get() > 0);
    }

    @Test
    void cachesTheRasterSoASecondCallDoesNotHitTheNetworkAgain() {
        String logoUrl = url("/logo.jpeg");

        byte[] first = provider.rasterFor(logoUrl, PaperWidth.MM_58);
        int hitsAfterFirstCall = requestCount.get();
        byte[] second = provider.rasterFor(logoUrl, PaperWidth.MM_58);

        assertSame(first, second);
        assertEquals(hitsAfterFirstCall, requestCount.get());
    }

    private String url(String path) {
        return "http://localhost:" + server.getAddress().getPort() + path;
    }

    private static byte[] readBundledLogoBytes() throws IOException {
        try (InputStream in = RemoteLogoProviderTest.class.getResourceAsStream("/assets/logo-amara.jpeg")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el recurso de prueba /assets/logo-amara.jpeg");
            }
            return in.readAllBytes();
        }
    }
}
