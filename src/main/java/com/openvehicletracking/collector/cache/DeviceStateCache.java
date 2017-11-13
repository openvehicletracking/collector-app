package com.openvehicletracking.collector.cache;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.openvehicletracking.core.DeviceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by oksuz on 28/09/2017.
 *
 */
public class DeviceStateCache {

    private static final String SHARED_MAP_NAME = "mts.devicestate";

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceStateCache.class);

    private final IMap<String, DeviceState> state;
    private static final DeviceStateCache INSTANCE = new DeviceStateCache();

    private DeviceStateCache() {
        HazelcastInstance client = HazelcastClient.newHazelcastClient(new ClientConfig().setGroupConfig(new GroupConfig("dev", "dev-pass")));
        state = client.getMap(SHARED_MAP_NAME);
    }

    public static DeviceStateCache getInstance() {
        return INSTANCE;
    }

    public void put(DeviceState deviceState) {
        if (deviceState.getDeviceId() != null && !"".equals(deviceState.getDeviceId().trim())) {
            state.put(deviceState.getDeviceId(), deviceState);
            LOGGER.debug("State put {}, {}", deviceState.getDeviceId(), deviceState);
        }
    }

    public DeviceState get(String deviceId) {
        return state.get(deviceId);
    }
}
