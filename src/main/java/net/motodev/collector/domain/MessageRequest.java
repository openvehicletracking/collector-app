package net.motodev.collector.domain;

import io.vertx.core.http.HttpServerRequest;
import net.motodev.core.GpsStatus;
import net.motodev.core.utility.DateUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public static final String DATE_FORMAT = "yyyy-MM-dd";
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
        if (getSize() == 0 && getFromDate() == null) {
            throw new Exception("date field is mandatory");
        }
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


    public GpsStatus getGpsStatus() {
        try {
            return GpsStatus.valueOf(request.getParam("gps"));
        } catch (IllegalArgumentException e) {
            LOGGER.info("Invalid status: " + request.getParam("gps"), e);
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
