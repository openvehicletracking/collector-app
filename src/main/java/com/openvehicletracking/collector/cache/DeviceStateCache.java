package com.openvehicletracking.collector.cache;

import com.openvehicletracking.core.DeviceState;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by oksuz on 28/09/2017.
 *
 */
public class DeviceStateCache {

    private static final ConcurrentHashMap<String, DeviceState> state = new ConcurrentHashMap<>();
    private static final DeviceStateCache INSTANCE = new DeviceStateCache();

    private DeviceStateCache() {}


    public static DeviceStateCache getInstance() {
        return INSTANCE;
    }

    public void put(DeviceState deviceState) {
        state.put(deviceState.getDeviceId(), deviceState);
    }

    public DeviceState get(String deviceId) {
        return state.get(deviceId);
    }
}
