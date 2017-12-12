package com.openvehicletracking.collector.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by oksuz on 08/10/2017.
 *
 */
public class DateHelper {

    private static final SimpleDateFormat ISO8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static Date fromISODateFormat(String date) {
        try {
            return ISO8601_DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toISODateFormat(Date d) {
        return ISO8601_DATE_FORMAT.format(d);
    }

}
