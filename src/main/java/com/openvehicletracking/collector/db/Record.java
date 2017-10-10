package com.openvehicletracking.collector.db;


import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by oksuz on 23/09/2017.
 *
 */
public class Record implements Serializable {

    private final MongoCollection collection;
    private final String record;
    private Query replaceQuery;
    private Query updateQuery;

    public Record(MongoCollection collection, JsonObject record) {
        Objects.requireNonNull(record, "recorc cannot be null");
        this.collection = collection;
        this.record = record.toString();
    }

    public Record(MongoCollection collection, JsonObject record, Query replaceQuery) {
        this(collection, record);
        this.replaceQuery = replaceQuery;
    }

    public MongoCollection getCollection() {
        return collection;
    }

    public JsonObject getRecord() {
        return new JsonObject(record);
    }

    public Query getReplaceQuery() {
        return replaceQuery;
    }

    public Record setReplaceQuery(Query replaceQuery) {
        this.replaceQuery = replaceQuery;
        return this;
    }

    public Query getUpdateQuery() {
        return updateQuery;
    }

    public Record setUpdateQuery(Query updateQuery) {
        this.updateQuery = updateQuery;
        return this;
    }
}
