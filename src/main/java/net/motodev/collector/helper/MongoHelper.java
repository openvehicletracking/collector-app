package net.motodev.collector.helper;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import net.motodev.core.GpsStatus;

import java.util.Date;

/**
 * Created by oksuz on 13/05/2017.
 */
public class MongoHelper {

    public static Query getLastMessagesQuery(int size, GpsStatus status, String deviceId, Date from, Date to) {

        FindOptions findOptions = new FindOptions();
        findOptions.setLimit(size);
        findOptions.setSort(new JsonObject().put("datetime", -1));
        JsonObject query = new JsonObject();

        if (null != from && null != to) {
            JsonObject datetimeCond = new JsonObject()
                    .put("$gte", from.getTime())
                    .put("$lte", to.getTime());
            query.put("datetime", datetimeCond);
        }

        query.put("deviceId", deviceId);
        if (status != null) {
            query.put("gpsStatus", status);
        }

        return new Query(findOptions, query);
    }


    public static class Query {

        public FindOptions getFindOptions() {
            return findOptions;
        }

        public JsonObject getQuery() {
            return query;
        }

        private FindOptions findOptions;
        private JsonObject query;

        public Query(FindOptions findOptions, JsonObject query) {
            this.query = query;
            this.findOptions = findOptions;
        }
    }
}
