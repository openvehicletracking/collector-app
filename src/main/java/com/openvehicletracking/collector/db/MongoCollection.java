package com.openvehicletracking.collector.db;

/**
 * Created by oksuz on 03/06/2017.
 */
public enum MongoCollection {

    MESSAGES("messages"),
    USERS("users"),
    COMMANDS("commands"),
    DEVICE_META("device_meta"),
    ALARMS("alarms"),
    PUBLIC_LOCATION_HASH("public_location_hash"),
    ACCESS_TOKENS("access_tokens");

    private final String col;

   MongoCollection(String col) {
        this.col = col;
    }

   public String getName() {
        return col;
   }

}
