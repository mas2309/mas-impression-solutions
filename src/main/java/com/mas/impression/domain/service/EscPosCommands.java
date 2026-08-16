package com.mas.impression.domain.service;

import java.nio.charset.Charset;

/**
 * Raw ESC/POS command bytes and the codepage used to encode ticket text. CP850 ("Multilingual
 * Latin I") is used instead of plain ASCII/UTF-8 because it covers the accented characters and
 * "ñ"/"¿"/"¡" that Spanish business names and product names routinely need, and is a codepage
 * virtually every ESC/POS-compatible thermal printer supports natively via {@link #SELECT_CODEPAGE_850}.
 */
public final class EscPosCommands {

    public static final Charset CHARSET = Charset.forName("Cp850");

    // Package-private: a public mutable array constant could be mutated in place by any caller
    // (SpotBugs MS_PKGPROTECT). Only TicketFormatter, in this same package, needs these.
    static final byte[] INIT = {0x1B, 0x40};
    static final byte[] ALIGN_LEFT = {0x1B, 0x61, 0x00};
    static final byte[] ALIGN_CENTER = {0x1B, 0x61, 0x01};
    static final byte[] BOLD_ON = {0x1B, 0x45, 0x01};
    static final byte[] BOLD_OFF = {0x1B, 0x45, 0x00};
    static final byte[] SELECT_CODEPAGE_850 = {0x1B, 0x74, 0x02};
    static final byte[] PARTIAL_CUT = {0x1D, 0x56, 0x01};

    /** {@code GS v 0} raster bit-image header, mode 0 (normal). Width/height bytes follow it. */
    static final byte[] RASTER_IMAGE_HEADER = {0x1D, 0x76, 0x30, 0x00};

    public static final byte LINE_FEED = 0x0A;

    private EscPosCommands() {
    }
}
