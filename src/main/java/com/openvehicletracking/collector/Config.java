package com.openvehicletracking.collector;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class Config {

    private static final Config instance = new Config();
    private JsonObject config;
    private final HashMap<String, Object> cached = new HashMap<>();

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
        return value instanceof String ? (String) value : def;
    }

    public int getInt(String key) {
        return getInt(key, -1);
    }

    public int getInt(String key, int def) {
        Object value = get(key);
        return value instanceof Integer ? (int) value : def;
    }

    public Object get(String key) {
        if (isCached(key)) {
            return getCached(key);
        }

        ArrayList<String> parts = new ArrayList<>(Arrays.asList(key.split("\\.")));
        Iterator<String> iterator = parts.iterator();
        JsonObject current = new JsonObject();

        while (iterator.hasNext()) {
            String next = iterator.next();
            if (!iterator.hasNext()) {
                Object retVal = current.isEmpty() ? config.getValue(next) : current.getValue(next);
                putToCache(key, retVal);
                return retVal;
            } else {
                current = current.isEmpty() ? config.getJsonObject(next) : current.getJsonObject(next);
            }
        }

        return null;
    }

    protected boolean isCached(String key) {
        return cached.containsKey(key);
    }

    protected Object getCached(String key) {
        return cached.get(key);
    }

    protected void putToCache(String key, Object val) {
        cached.putIfAbsent(key, val);
    }

}
