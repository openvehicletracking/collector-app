package com.openvehicletracking.collector;

/**
 * Created by oksuz on 12/09/2017.
 */
public class AppConstants {

    public static final String MONGO_POOL_NAME = "collector_pool";
    public static final String HEADER_ACCESS_TOKEN = "x-access-token";

    public static class Events {

        public static final String NEW_RAW_MESSAGE = "new.rawMessage";
        public static final String NEW_QUERY = "database.query";
        public static final String PERSIST = "database.persist";
        public static final String UPDATE = "database.update";
        public static final String DELETE = "database.delete";
        public static String ALARM = "device.alarm";
        public static String NEW_SMS = "new.sms";
    }



}
