package com.mas.impression.infrastructure.asset;

import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.service.LogoRasterConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fetches and rasterizes a tenant's own logo from the {@code logoUrl} sent with each print
 * request, so a business prints its own branding instead of the bundled default in
 * {@link LogoAssetProvider}. Unlike that bundled asset, this IS tenant data, downloaded over the
 * network — the raster is cached per (url, paper width) so a busy register doesn't refetch and
 * reconvert the same image on every sale. Any failure (missing URL, network error, unreadable
 * image) returns {@code null} instead of throwing, so a broken logo link degrades to "no
 * per-tenant logo" rather than failing the print job.
 */
@Component
public class RemoteLogoProvider {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteLogoProvider.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final LogoRasterConverter converter;
    private final HttpClient httpClient;
    private final Map<String, byte[]> rasterCache = new ConcurrentHashMap<>();

    public RemoteLogoProvider(LogoRasterConverter converter) {
        this.converter = converter;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
    }

    /** Returns the raster for {@code logoUrl}, or {@code null} if there's none or it couldn't be resolved. */
    public byte[] rasterFor(String logoUrl, PaperWidth paperWidth) {
        if (logoUrl == null || logoUrl.isBlank()) {
            return null;
        }

        String cacheKey = logoUrl + '|' + paperWidth;
        byte[] cached = rasterCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        byte[] raster = fetchAndConvert(logoUrl, paperWidth);
        if (raster != null) {
            rasterCache.put(cacheKey, raster);
        }
        return raster;
    }

    private byte[] fetchAndConvert(String logoUrl, PaperWidth paperWidth) {
        try {
            BufferedImage image = downloadImage(logoUrl);
            if (image == null) {
                LOG.warn("El logo del negocio no se pudo decodificar como imagen ({})", logoUrl);
                return null;
            }
            return converter.convert(image, paperWidth);
        } catch (IOException ex) {
            LOG.warn("No se pudo descargar el logo del negocio ({}): {}", logoUrl, ex.getMessage());
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.warn("Descarga del logo del negocio interrumpida ({})", logoUrl);
            return null;
        } catch (IllegalArgumentException ex) {
            LOG.warn("URL de logo inválida ({}): {}", logoUrl, ex.getMessage());
            return null;
        }
    }

    private BufferedImage downloadImage(String logoUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(logoUrl))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            LOG.warn("El logo del negocio respondió HTTP {} ({})", response.statusCode(), logoUrl);
            return null;
        }
        return ImageIO.read(new ByteArrayInputStream(response.body()));
    }
}
