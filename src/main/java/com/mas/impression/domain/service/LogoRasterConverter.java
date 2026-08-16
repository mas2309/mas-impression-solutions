package com.mas.impression.domain.service;

import com.mas.impression.domain.model.PaperWidth;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * Converts a source logo image into ESC/POS {@code GS v 0} raster bit-image bytes, scaled to a
 * fraction of the paper width and thresholded to 1-bit black/white (thermal printers have no
 * grayscale). Pure: no file/network I/O — the caller is responsible for loading the source image
 * (once, at startup) and caching the result per {@link PaperWidth}.
 */
public class LogoRasterConverter {

    private static final double LOGO_WIDTH_RATIO = 0.6;
    private static final int BITS_PER_BYTE = 8;
    private static final int BYTE_MASK = 0xFF;
    private static final int MSB_MASK = 0x80;
    private static final int LUMINANCE_THRESHOLD = 128;
    private static final double RED_WEIGHT = 0.299;
    private static final double GREEN_WEIGHT = 0.587;
    private static final double BLUE_WEIGHT = 0.114;

    public byte[] convert(BufferedImage source, PaperWidth paperWidth) {
        int targetWidth = (int) (paperWidth.dotsWidth() * LOGO_WIDTH_RATIO);
        BufferedImage scaled = scale(source, targetWidth);
        return toRasterCommand(scaled);
    }

    private static BufferedImage scale(BufferedImage source, int targetWidth) {
        int targetHeight = Math.max(1, source.getHeight() * targetWidth / source.getWidth());
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return scaled;
    }

    private static byte[] toRasterCommand(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int widthBytes = (width + BITS_PER_BYTE - 1) / BITS_PER_BYTE;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(EscPosCommands.RASTER_IMAGE_HEADER);
        writeLittleEndianShort(out, widthBytes);
        writeLittleEndianShort(out, height);
        writePixelData(out, image, widthBytes, width, height);
        return out.toByteArray();
    }

    private static void writeLittleEndianShort(ByteArrayOutputStream out, int value) {
        out.write(value & BYTE_MASK);
        out.write((value >> BITS_PER_BYTE) & BYTE_MASK);
    }

    private static void writePixelData(ByteArrayOutputStream out, BufferedImage image,
            int widthBytes, int width, int height) {
        for (int y = 0; y < height; y++) {
            byte[] row = new byte[widthBytes];
            for (int x = 0; x < width; x++) {
                if (isDark(image.getRGB(x, y))) {
                    row[x / BITS_PER_BYTE] |= (byte) (MSB_MASK >> (x % BITS_PER_BYTE));
                }
            }
            out.writeBytes(row);
        }
    }

    private static boolean isDark(int rgb) {
        int red = (rgb >> (2 * BITS_PER_BYTE)) & BYTE_MASK;
        int green = (rgb >> BITS_PER_BYTE) & BYTE_MASK;
        int blue = rgb & BYTE_MASK;
        double luminance = RED_WEIGHT * red + GREEN_WEIGHT * green + BLUE_WEIGHT * blue;
        return luminance < LUMINANCE_THRESHOLD;
    }
}
