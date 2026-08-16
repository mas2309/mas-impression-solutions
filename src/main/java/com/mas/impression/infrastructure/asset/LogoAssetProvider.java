package com.mas.impression.infrastructure.asset;

import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.service.LogoRasterConverter;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads the bundled ticket logo once at startup and caches its {@code GS v 0} raster form per
 * {@link PaperWidth} — the logo is a static build asset, not tenant data, so unlike sale/branding
 * text it legitimately lives in the agent rather than being relayed on every print request.
 */
@Component
public class LogoAssetProvider {

    private static final String LOGO_RESOURCE = "/assets/logo-amara.jpeg";

    private final LogoRasterConverter converter;
    private final BufferedImage logoImage;
    private final Map<PaperWidth, byte[]> rasterCache = new ConcurrentHashMap<>();

    public LogoAssetProvider(LogoRasterConverter converter) {
        this.converter = converter;
        this.logoImage = loadLogoImage();
    }

    public byte[] rasterFor(PaperWidth paperWidth) {
        return rasterCache.computeIfAbsent(paperWidth, width -> converter.convert(logoImage, width));
    }

    private static BufferedImage loadLogoImage() {
        try (InputStream in = LogoAssetProvider.class.getResourceAsStream(LOGO_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el recurso empaquetado " + LOGO_RESOURCE);
            }
            return ImageIO.read(in);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo cargar el logo del ticket", ex);
        }
    }
}
