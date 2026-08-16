package com.mas.impression.infrastructure.printer.bluetooth;

import java.io.IOException;
import java.util.List;

/**
 * Talks to whatever COM ports the OS currently exposes. A Bluetooth SPP thermal printer becomes
 * one of these automatically once paired through Windows' own Bluetooth settings — no app-level
 * pairing or extra driver is needed here, only picking the right port name.
 */
public interface BluetoothPortGateway {

    List<String> listPortNames();

    BluetoothPortHandle open(String portName) throws IOException;
}
