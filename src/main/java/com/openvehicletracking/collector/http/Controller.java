package com.openvehicletracking.collector.http;


import io.vertx.core.json.JsonObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by oksuz on 07/10/2017.
 *
 */
public class Controller {

    private static final ConcurrentHashMap<String, Object> instances = new ConcurrentHashMap<>();
    private static JsonObject config = new JsonObject();

    public static void setConfig(JsonObject config) {
        Controller.config = new JsonObject(config.toString());
    }

    @SuppressWarnings("unchecked")
    public static <T> T of(Class<T> tClass) {

        if (instances.containsKey(tClass.getCanonicalName())) {
            return tClass.cast(instances.get(tClass.getCanonicalName()));
        }

        try {
            Constructor constructor = tClass.getConstructor(JsonObject.class);
            T instance = (T) constructor.newInstance(config);
            instances.putIfAbsent(tClass.getCanonicalName(), instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
