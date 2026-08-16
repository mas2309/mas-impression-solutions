package com.mas.impression.infrastructure.asset;

import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.service.LogoRasterConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogoAssetProviderTest {

    private final LogoAssetProvider provider = new LogoAssetProvider(new LogoRasterConverter());

    @Test
    void loadsBundledLogoAndProducesRasterForEachPaperWidth() {
        byte[] raster58 = provider.rasterFor(PaperWidth.MM_58);
        byte[] raster80 = provider.rasterFor(PaperWidth.MM_80);

        assertTrue(raster58.length > 0);
        assertTrue(raster80.length > 0);
        assertNotEquals(raster58.length, raster80.length);
    }

    @Test
    void cachesRasterPerPaperWidth() {
        byte[] first = provider.rasterFor(PaperWidth.MM_58);
        byte[] second = provider.rasterFor(PaperWidth.MM_58);

        assertSame(first, second);
    }
}
