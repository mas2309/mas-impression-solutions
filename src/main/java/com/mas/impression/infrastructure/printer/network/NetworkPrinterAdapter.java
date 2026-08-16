package com.mas.impression.infrastructure.printer.network;

import com.mas.impression.domain.exception.PrinterNotConnectedException;
import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.model.PrinterConnectionType;
import com.mas.impression.domain.model.PrinterState;
import com.mas.impression.domain.port.PrinterPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Reaches a network-connected thermal printer over a raw TCP socket (the near-universal
 * "port 9100" raw-print convention). A fresh socket is opened per print job rather than kept
 * alive, since the printer may be power-cycled between sales and a stale long-lived connection
 * would otherwise fail silently.
 */
@Component
public class NetworkPrinterAdapter implements PrinterPort {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkPrinterAdapter.class);

    private final AtomicReference<PrinterState> lastState =
            new AtomicReference<>(new PrinterState(false, PrinterConnectionType.NETWORK, null, null));

    private volatile Socket activeSocket;

    @Override
    public void connect(PrinterConnectionConfig config) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(config.host(), config.port()), config.connectTimeoutMillis());
            this.activeSocket = socket;
            recordConnected();
        } catch (IOException ex) {
            recordFailure(ex.getMessage());
            throw new PrinterNotConnectedException(
                    "No se pudo conectar a la impresora de red " + config.host() + ":" + config.port(), ex);
        }
    }

    @Override
    public void write(byte[] data) {
        Socket socket = this.activeSocket;
        if (socket == null || socket.isClosed()) {
            recordFailure("No hay una conexión activa con la impresora.");
            throw new PrinterNotConnectedException("No hay una conexión activa con la impresora.");
        }
        try {
            socket.getOutputStream().write(data);
            socket.getOutputStream().flush();
            recordPrinted();
        } catch (IOException ex) {
            recordFailure(ex.getMessage());
            throw new PrinterNotConnectedException("Error al enviar datos a la impresora de red.", ex);
        }
    }

    @Override
    public PrinterState status() {
        return lastState.get();
    }

    @Override
    public void disconnect() {
        Socket socket = this.activeSocket;
        this.activeSocket = null;
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ex) {
            LOG.debug("Error cerrando el socket de la impresora, se descarta de todas formas.", ex);
        }
    }

    private void recordConnected() {
        PrinterState previous = lastState.get();
        lastState.set(new PrinterState(true, PrinterConnectionType.NETWORK, null, previous.lastPrintAt()));
    }

    private void recordPrinted() {
        lastState.set(new PrinterState(true, PrinterConnectionType.NETWORK, null, LocalDateTime.now()));
    }

    private void recordFailure(String message) {
        PrinterState previous = lastState.get();
        lastState.set(new PrinterState(false, PrinterConnectionType.NETWORK, message, previous.lastPrintAt()));
    }
}
