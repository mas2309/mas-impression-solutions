package com.mas.impression.infrastructure.config;

import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.model.PrinterConnectionType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Where and how this install reaches its printer, and what paper it's loaded with. In M1 this
 * is the single source of truth (static config file); a later milestone persists an
 * admin-editable copy, without changing the shape consumers depend on.
 */
@ConfigurationProperties(prefix = "agent.printer")
public class PrinterProperties {

    private PrinterConnectionType connectionType = PrinterConnectionType.NETWORK;
    private PaperWidth paperWidth = PaperWidth.MM_58;
    private Network network = new Network();
    private Usb usb = new Usb();
    private Bluetooth bluetooth = new Bluetooth();

    public PrinterConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(PrinterConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public PaperWidth getPaperWidth() {
        return paperWidth;
    }

    public void setPaperWidth(PaperWidth paperWidth) {
        this.paperWidth = paperWidth;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Usb getUsb() {
        return usb;
    }

    public void setUsb(Usb usb) {
        this.usb = usb;
    }

    public Bluetooth getBluetooth() {
        return bluetooth;
    }

    public void setBluetooth(Bluetooth bluetooth) {
        this.bluetooth = bluetooth;
    }

    public PrinterConnectionConfig toConnectionConfig() {
        return new PrinterConnectionConfig(
                connectionType, paperWidth, network.getHost(), network.getPort(), network.getConnectTimeoutMillis(),
                usb.getPrintServiceName(), bluetooth.getPortName());
    }

    public static class Network {

        private String host = "127.0.0.1";
        private int port = 9100;
        private int connectTimeoutMillis = 3000;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getConnectTimeoutMillis() {
            return connectTimeoutMillis;
        }

        public void setConnectTimeoutMillis(int connectTimeoutMillis) {
            this.connectTimeoutMillis = connectTimeoutMillis;
        }
    }

    public static class Usb {

        private String printServiceName = "";

        public String getPrintServiceName() {
            return printServiceName;
        }

        public void setPrintServiceName(String printServiceName) {
            this.printServiceName = printServiceName;
        }
    }

    public static class Bluetooth {

        private String portName = "";

        public String getPortName() {
            return portName;
        }

        public void setPortName(String portName) {
            this.portName = portName;
        }
    }
}
