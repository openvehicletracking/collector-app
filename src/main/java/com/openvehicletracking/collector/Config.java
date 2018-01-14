package com.openvehicletracking.collector;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class Config {

    private static final Config instance = new Config();
    private JsonObject config;

    public static Config getInstance() {
        return instance;
    }

    public Config load(JsonObject conf) {
        this.config = conf;
        return this;
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String def) {
        Object value = get(key);
        return value != null && value instanceof String ? (String) value : def;
    }


    public int getInt(String key) {
        return getInt(key, -1);
    }

    public int getInt(String key, int def) {
        Object value = get(key);
        return value != null && value instanceof Integer ? (int) value : def;
    }

    protected Object get(String key) {
        ArrayList<String> parts = new ArrayList<>(Arrays.asList(key.split("\\.")));
        Iterator<String> iterator = parts.iterator();
        JsonObject current = new JsonObject();

        while (iterator.hasNext()) {
            String next = iterator.next();
            if (!iterator.hasNext()) {
                return current.isEmpty() ? config.getValue(next) : current.getValue(next);
            } else {
                current = current.isEmpty() ? config.getJsonObject(next) : current.getJsonObject(next);
            }
        }

        return null;
    }

}
