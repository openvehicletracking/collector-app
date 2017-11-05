package com.openvehicletracking.collector.cache;

import com.google.gson.JsonParser;
import com.openvehicletracking.core.DeviceState;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

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
        if (deviceState.getDeviceId() != null && !"".equals(deviceState.getDeviceId().trim())) {
            state.put(deviceState.getDeviceId(), deviceState);
        }
    }

    public DeviceState get(String deviceId) {
        return state.get(deviceId);
    }

    public JsonArray getAllAsJson() {
        JsonArray data = new JsonArray();
        state.forEach((deviceId, state) -> data.add(new JsonObject(state.toJson().toString())));
        return data;
    }

    public void load(JsonArray data) {
        state.clear();
        JsonParser parser = new JsonParser();
        for (int i = 0; i < data.size(); i++) {
            JsonObject deviceStateJson = data.getJsonObject(i);
            DeviceState deviceState = DeviceState.fromJson(parser.parse(deviceStateJson.toString()).getAsJsonObject());
            put(deviceState);
        }
    }
}
