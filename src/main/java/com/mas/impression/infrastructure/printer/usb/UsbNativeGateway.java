package com.mas.impression.infrastructure.printer.usb;

import org.springframework.stereotype.Component;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import java.util.Arrays;
import java.util.List;

/**
 * The only class in this package that actually calls {@code javax.print}. Kept deliberately thin
 * so {@link UsbPrinterAdapter}'s real logic (validation, lifecycle, error translation) can be
 * unit-tested against a fake {@link UsbPrintServiceGateway} instead of real OS print services;
 * this class is excluded from the coverage gate accordingly.
 */
@Component
public class UsbNativeGateway implements UsbPrintServiceGateway {

    @Override
    public List<String> listPrintServiceNames() {
        return Arrays.stream(lookupServices()).map(PrintService::getName).toList();
    }

    @Override
    public boolean printServiceExists(String printServiceName) {
        return findService(printServiceName) != null;
    }

    @Override
    public void print(String printServiceName, byte[] data) throws PrintException {
        PrintService service = findService(printServiceName);
        if (service == null) {
            throw new PrintException("No se encontró la impresora USB: " + printServiceName);
        }
        DocPrintJob job = service.createPrintJob();
        job.print(new SimpleDoc(data, DocFlavor.BYTE_ARRAY.AUTOSENSE, null), null);
    }

    private static PrintService findService(String printServiceName) {
        return Arrays.stream(lookupServices())
                .filter(service -> service.getName().equals(printServiceName))
                .findFirst()
                .orElse(null);
    }

    private static PrintService[] lookupServices() {
        return PrintServiceLookup.lookupPrintServices(DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
    }
}
