package app;

import java.io.Serializable;

public class BootstrapServentInfo implements Serializable {

    private final String ipAddress;
    private final int listenerPort;

    public BootstrapServentInfo(String ipAddress, int listenerPort) {
        this.ipAddress = ipAddress;
        this.listenerPort = listenerPort;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getListenerPort() {
        return listenerPort;
    }
}
