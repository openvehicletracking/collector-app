package com.openvehicletracking.collector.http.domain;

import io.vertx.ext.web.RoutingContext;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by oksuz on 18/11/2017.
 */
public class HashCreateRequest {

    private final RoutingContext context;
    private static final int EXPIRE_IN_MINUTES = 30;

    public HashCreateRequest(RoutingContext context) {
        this.context = context;
    }

    public String getDeviceId() {
        return context.request().getParam("deviceId");
    }

    public Date getExpireDate() {
        long expireDate;
        if (context.getBody().length() != 0 && context.getBodyAsJson().containsKey("expireDate")) {
            expireDate = context.getBodyAsJson().getLong("expireDate");
            return new Date(expireDate);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, EXPIRE_IN_MINUTES);
        return calendar.getTime();
    }

    public String createHashForRequest() {
        return UUID.randomUUID().toString().replace("-", "").substring(1,7);
    }

    public String getDeviceLabel() {
        UserDevice userDevice = context.get("device");
        return userDevice.getLabel();
    }
}
