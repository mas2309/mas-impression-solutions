package com.mas.impression.domain.model;

/**
 * Supported thermal paper widths, with their raster width in dots at 203dpi
 * and the character budget for Font-A monospace text at that width.
 */
public enum PaperWidth {

    MM_58(384, 32),
    MM_80(576, 48);

    private final int dotsWidth;
    private final int charsPerLine;

    PaperWidth(int dotsWidth, int charsPerLine) {
        this.dotsWidth = dotsWidth;
        this.charsPerLine = charsPerLine;
    }

    public int dotsWidth() {
        return dotsWidth;
    }

    public int charsPerLine() {
        return charsPerLine;
    }
}
