package com.openvehicletracking.collector.database;


import com.openvehicletracking.core.json.GsonFactory;
import com.openvehicletracking.core.json.JsonSerializeable;
import io.vertx.core.json.JsonObject;


import java.util.Objects;

/**
 * Created by oksuz on 23/09/2017.
 *
 */
final public class Record implements JsonSerializeable {

    private final MongoCollection collection;
    private final String record;

    public Record(MongoCollection collection, JsonObject record) {
        Objects.requireNonNull(record, "record cannot be null");
        this.collection = collection;
        this.record = record.toString();
    }

    public MongoCollection getCollection() {
        return collection;
    }

    public JsonObject getRecord() {
        return new JsonObject(record);
    }

    public static Record fromJson(String json) {
        return GsonFactory.getGson().fromJson(json, Record.class);
    }

    @Override
    public String asJson() {
        return GsonFactory.getGson().toJson(this);
    }
}
