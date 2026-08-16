package com.mas.impression.infrastructure.printer.bluetooth;

import java.io.Closeable;
import java.io.IOException;

/**
 * An open serial connection to a paired Bluetooth printer's COM port.
 */
public interface BluetoothPortHandle extends Closeable {

    void write(byte[] data) throws IOException;
}
