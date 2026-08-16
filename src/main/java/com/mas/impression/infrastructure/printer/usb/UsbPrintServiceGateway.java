package com.mas.impression.infrastructure.printer.usb;

import javax.print.PrintException;
import java.util.List;

/**
 * Talks to whatever the OS already knows about USB printers. On Windows this is the print
 * spooler queue (a USB thermal printer installed with its own driver, or "Generic / Text Only",
 * shows up here) — deliberately not raw USB/libusb access, which would require replacing the
 * device's Windows driver (e.g. via Zadig) on every till, an unreasonable install burden for a
 * retail deployment.
 */
public interface UsbPrintServiceGateway {

    List<String> listPrintServiceNames();

    boolean printServiceExists(String printServiceName);

    void print(String printServiceName, byte[] data) throws PrintException;
}
