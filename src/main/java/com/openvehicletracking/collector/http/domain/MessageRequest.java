package com.openvehicletracking.collector.http.domain;


import com.openvehicletracking.core.GpsStatus;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by oksuz on 08/10/2017.
 *
 */

public class MessageRequest {

    private HttpServerRequest request;

    public static final String DATE_FORMAT = "yyyyMMddHHmmss";
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageRequest.class);

    public static ThreadLocal<SimpleDateFormat> requestDateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DATE_FORMAT);
        }
    };

    public MessageRequest(HttpServerRequest request) throws Exception {
        this.request = request;
        validateRequest();
    }

    private void validateRequest() throws Exception {
        if (getFromDate() == null) {
            throw new Exception("from (date) field is mandatory with format " + DATE_FORMAT);
        }
    }

    public Date getFromDate() throws ParseException {
        Date d = getDateField("from");
        if (d != null) {
            return d;
        }

        return null;
    }

    public int getSize() {
        try {
            if (request.getParam("size") != null) {
                return Integer.parseInt(request.getParam("size"));
            }
        } catch (NumberFormatException ignored) {}

        return 0;
    }


    public Date getToDate() throws ParseException {
        Date d = getDateField("to");
        if (d != null) {
            return d;
        }

        return null;
    }

    public Date getDateField(String name) throws ParseException {
        return (request.getParam(name) != null) ? requestDateFormat.get().parse(request.getParam(name)) : null;
    }


    public GpsStatus getGpsStatus() {
        if (request.getParam("gps") != null) {
            try {
                return GpsStatus.valueOf(request.getParam("gps"));
            } catch (IllegalArgumentException e) {
                LOGGER.info("Invalid status: " + request.getParam("gps"), e);
            }
        }

        return null;
    }

    public String getDeviceId() {
        return request.getParam("deviceId");
    }


    public void setRequest(HttpServerRequest request) {
        this.request = request;
    }
}