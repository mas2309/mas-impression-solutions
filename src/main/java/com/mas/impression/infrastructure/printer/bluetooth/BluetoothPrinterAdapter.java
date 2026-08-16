package com.mas.impression.infrastructure.printer.bluetooth;

import com.mas.impression.domain.exception.PrinterNotConnectedException;
import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.model.PrinterConnectionType;
import com.mas.impression.domain.model.PrinterState;
import com.mas.impression.domain.port.PrinterPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Reaches a Bluetooth-paired thermal printer over its virtual serial (COM) port. Windows creates
 * this port automatically once the printer is paired through the OS Bluetooth settings — this
 * adapter only opens/writes/closes it, it never handles pairing itself.
 */
@Component
public class BluetoothPrinterAdapter implements PrinterPort {

    private static final Logger LOG = LoggerFactory.getLogger(BluetoothPrinterAdapter.class);

    private final BluetoothPortGateway gateway;

    private final AtomicReference<PrinterState> lastState =
            new AtomicReference<>(new PrinterState(false, PrinterConnectionType.BLUETOOTH, null, null));

    private volatile BluetoothPortHandle activeHandle;

    public BluetoothPrinterAdapter(BluetoothPortGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public void connect(PrinterConnectionConfig config) {
        String portName = config.bluetoothPortName();
        if (portName == null || portName.isBlank()) {
            recordFailure("No se configuró un puerto Bluetooth.");
            throw new PrinterNotConnectedException("No se configuró un puerto Bluetooth.");
        }
        try {
            this.activeHandle = gateway.open(portName);
            recordConnected();
        } catch (IOException ex) {
            recordFailure(ex.getMessage());
            throw new PrinterNotConnectedException("No se pudo conectar al puerto Bluetooth " + portName, ex);
        }
    }

    @Override
    public void write(byte[] data) {
        BluetoothPortHandle handle = this.activeHandle;
        if (handle == null) {
            recordFailure("No hay una conexión Bluetooth activa.");
            throw new PrinterNotConnectedException("No hay una conexión Bluetooth activa.");
        }
        try {
            handle.write(data);
            recordPrinted();
        } catch (IOException ex) {
            recordFailure(ex.getMessage());
            throw new PrinterNotConnectedException("Error al enviar datos por Bluetooth.", ex);
        }
    }

    @Override
    public PrinterState status() {
        return lastState.get();
    }

    @Override
    public void disconnect() {
        BluetoothPortHandle handle = this.activeHandle;
        this.activeHandle = null;
        if (handle == null) {
            return;
        }
        try {
            handle.close();
        } catch (IOException ex) {
            LOG.debug("Error cerrando el puerto Bluetooth, se descarta de todas formas.", ex);
        }
    }

    private void recordConnected() {
        PrinterState previous = lastState.get();
        lastState.set(new PrinterState(true, PrinterConnectionType.BLUETOOTH, null, previous.lastPrintAt()));
    }

    private void recordPrinted() {
        lastState.set(new PrinterState(true, PrinterConnectionType.BLUETOOTH, null, LocalDateTime.now()));
    }

    private void recordFailure(String message) {
        PrinterState previous = lastState.get();
        lastState.set(new PrinterState(false, PrinterConnectionType.BLUETOOTH, message, previous.lastPrintAt()));
    }
}
