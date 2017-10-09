package com.openvehicletracking.collector.http;


import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by oksuz on 07/10/2017.
 *
 */
public class Controller {

    private static final ConcurrentHashMap<String, Object> instances = new ConcurrentHashMap<>();

    public static <T> T of(Class<T> tClass) {

        if (instances.containsKey(tClass.getCanonicalName())) {
            return tClass.cast(instances.get(tClass.getCanonicalName()));
        }

        try {
            T instance = tClass.newInstance();
            instances.putIfAbsent(tClass.getCanonicalName(), instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
