package com.openvehicletracking.collector.connection;

import com.openvehicletracking.core.Device;

import java.util.Objects;

public class ActiveDeviceConnection {

    private final String ip;
    private final Device device;

    public ActiveDeviceConnection(String ip, Device device) {
        this.ip = ip;
        this.device = device;
    }

    public String getIp() {
        return ip;
    }

    public Device getDevice() {
        return device;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActiveDeviceConnection)) return false;

        ActiveDeviceConnection that = (ActiveDeviceConnection) o;

        return Objects.equals(that.ip, this.ip) && Objects.equals(that.getDevice().getId(), this.getDevice().getId());
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + (device != null ? device.hashCode() : 0);
        return result;
    }
}
