package com.mas.impression.infrastructure.printer.usb;

import com.mas.impression.domain.exception.PrinterNotConnectedException;
import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.model.PrinterConnectionType;
import com.mas.impression.domain.model.PrinterState;
import com.mas.impression.domain.port.PrinterPort;
import org.springframework.stereotype.Component;

import javax.print.PrintException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Reaches a USB-connected thermal printer already registered as a Windows print queue. There is
 * no persistent "connection" to hold open here — {@link #connect} just validates the configured
 * queue exists, and {@link #write} submits a fresh raw print job each time, which is how the
 * Windows spooler itself works.
 */
@Component
public class UsbPrinterAdapter implements PrinterPort {

    private final UsbPrintServiceGateway gateway;

    private final AtomicReference<PrinterState> lastState =
            new AtomicReference<>(new PrinterState(false, PrinterConnectionType.USB, null, null));

    private volatile String activeServiceName;

    public UsbPrinterAdapter(UsbPrintServiceGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public void connect(PrinterConnectionConfig config) {
        String serviceName = config.usbPrintServiceName();
        if (serviceName == null || serviceName.isBlank() || !gateway.printServiceExists(serviceName)) {
            recordFailure("La impresora USB configurada no está disponible: " + serviceName);
            throw new PrinterNotConnectedException(
                    "La impresora USB configurada no está disponible: " + serviceName);
        }
        this.activeServiceName = serviceName;
        recordConnected();
    }

    @Override
    public void write(byte[] data) {
        String serviceName = this.activeServiceName;
        if (serviceName == null) {
            recordFailure("No hay una impresora USB seleccionada.");
            throw new PrinterNotConnectedException("No hay una impresora USB seleccionada.");
        }
        try {
            gateway.print(serviceName, data);
            recordPrinted();
        } catch (PrintException ex) {
            recordFailure(ex.getMessage());
            throw new PrinterNotConnectedException("Error al enviar datos a la impresora USB.", ex);
        }
    }

    @Override
    public PrinterState status() {
        return lastState.get();
    }

    @Override
    public void disconnect() {
        this.activeServiceName = null;
    }

    private void recordConnected() {
        PrinterState previous = lastState.get();
        lastState.set(new PrinterState(true, PrinterConnectionType.USB, null, previous.lastPrintAt()));
    }

    private void recordPrinted() {
        lastState.set(new PrinterState(true, PrinterConnectionType.USB, null, LocalDateTime.now()));
    }

    private void recordFailure(String message) {
        PrinterState previous = lastState.get();
        lastState.set(new PrinterState(false, PrinterConnectionType.USB, message, previous.lastPrintAt()));
    }
}
