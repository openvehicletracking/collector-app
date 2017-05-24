package net.motodev.collector.domain;

import io.vertx.core.http.HttpServerRequest;
import net.motodev.core.utility.DateUtility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by oksuz on 14/05/2017.
 *
 */
public class MessageRequest {

    private HttpServerRequest request;

    public static final int DEFAULT_LIMIT = 100;
    public static final int DEFAULT_STATUS = -1;
    public static final String DATE_FORMAT = "yyyyMMdd";
    public static ThreadLocal<SimpleDateFormat> requestDateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DATE_FORMAT);
        }
    };

    public MessageRequest(HttpServerRequest request) {
        this.request = request;
    }

    public int getSize() {
        try {
            return (request.getParam("size") != null) ? Integer.parseInt(request.getParam("size")) : DEFAULT_LIMIT;
        } catch (NumberFormatException ignored) {}

        return DEFAULT_LIMIT;
    }

    public Date getFromDate() throws ParseException {
        Date d = (request.getParam("date") != null) ? requestDateFormat.get().parse(request.getParam("date")) : null;
        if (d != null) {
            return DateUtility.getBeginningOfDay(d);
        }

        return null;
    }


    public Date getToDate() throws ParseException {
        Date d = (request.getParam("date") != null) ? requestDateFormat.get().parse(request.getParam("date")) : null;
        if (d != null) {
            return DateUtility.getEndOfDay(d);
        }

        return null;
    }


    public int getStatus() {
        try {
            return (request.getParam("messageStatus") != null) ? Integer.parseInt(request.getParam("messageStatus")) : DEFAULT_STATUS;
        } catch (NumberFormatException ignored) {}

        return  DEFAULT_STATUS;
    }

    public String getDeviceId() {
        return request.getParam("deviceId");
    }


    public void setRequest(HttpServerRequest request) {
        this.request = request;
    }
}
