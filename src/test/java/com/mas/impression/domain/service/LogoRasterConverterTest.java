package com.mas.impression.domain.service;

import com.mas.impression.domain.model.PaperWidth;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogoRasterConverterTest {

    private static final int HEADER_LENGTH = 8;

    private final LogoRasterConverter converter = new LogoRasterConverter();

    @Test
    void outputStartsWithRasterImageHeader() {
        byte[] result = converter.convert(solidImage(20, 20, Color.BLACK), PaperWidth.MM_58);

        byte[] header = Arrays.copyOfRange(result, 0, EscPosCommands.RASTER_IMAGE_HEADER.length);
        assertTrue(Arrays.equals(EscPosCommands.RASTER_IMAGE_HEADER, header));
    }

    @Test
    void scaledWidthNeverExceedsPaperDotsWidth() {
        byte[] result = converter.convert(solidImage(1000, 200, Color.BLACK), PaperWidth.MM_58);

        int widthBytes = readLittleEndianShort(result, 4);
        assertTrue(widthBytes * 8 <= PaperWidth.MM_58.dotsWidth());
    }

    @Test
    void outputLengthMatchesEncodedWidthAndHeight() {
        byte[] result = converter.convert(solidImage(200, 80, Color.BLACK), PaperWidth.MM_80);

        int widthBytes = readLittleEndianShort(result, 4);
        int height = readLittleEndianShort(result, 6);
        assertEquals(HEADER_LENGTH + widthBytes * height, result.length);
    }

    @Test
    void whiteImageProducesNoSetBits() {
        byte[] result = converter.convert(solidImage(50, 50, Color.WHITE), PaperWidth.MM_58);

        for (byte pixelByte : Arrays.copyOfRange(result, HEADER_LENGTH, result.length)) {
            assertEquals(0, pixelByte);
        }
    }

    @Test
    void blackImageProducesSetBits() {
        byte[] result = converter.convert(solidImage(50, 50, Color.BLACK), PaperWidth.MM_58);

        boolean anySet = false;
        for (byte pixelByte : Arrays.copyOfRange(result, HEADER_LENGTH, result.length)) {
            anySet = anySet || pixelByte != 0;
        }
        assertTrue(anySet);
    }

    private static int readLittleEndianShort(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }

    private static BufferedImage solidImage(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        return image;
    }
}
