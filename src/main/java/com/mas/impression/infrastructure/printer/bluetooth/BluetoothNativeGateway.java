package com.mas.impression.infrastructure.printer.bluetooth;

import com.fazecast.jSerialComm.SerialPort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * The only class in this package that actually calls jSerialComm. Kept deliberately thin so
 * {@link BluetoothPrinterAdapter}'s real logic can be unit-tested against a fake
 * {@link BluetoothPortGateway} instead of a real serial port; this class is excluded from the
 * coverage gate accordingly.
 */
@Component
public class BluetoothNativeGateway implements BluetoothPortGateway {

    @Override
    public List<String> listPortNames() {
        return Arrays.stream(SerialPort.getCommPorts()).map(SerialPort::getSystemPortName).toList();
    }

    @Override
    public BluetoothPortHandle open(String portName) throws IOException {
        SerialPort port = SerialPort.getCommPort(portName);
        if (!port.openPort()) {
            throw new IOException("No se pudo abrir el puerto " + portName);
        }
        return new JSerialCommHandle(port);
    }

    private static final class JSerialCommHandle implements BluetoothPortHandle {

        private final SerialPort port;

        private JSerialCommHandle(SerialPort port) {
            this.port = port;
        }

        @Override
        public void write(byte[] data) throws IOException {
            OutputStream out = port.getOutputStream();
            out.write(data);
            out.flush();
        }

        @Override
        public void close() {
            port.closePort();
        }
    }
}
