package com.openvehicletracking.collector.db;

import com.openvehicletracking.core.GsonFactory;
import com.openvehicletracking.core.JsonSerializeable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;

import java.util.Objects;

/**
 * Created by oksuz on 23/09/2017.
 *
 */
public class Query implements JsonSerializeable {

    private FindOptions findOptions = new FindOptions();
    private String query = "{}";
    private MongoCollection collection;
    private boolean findOne = false;


    public Query(MongoCollection collection, JsonObject query) {
        this(collection);
        Objects.requireNonNull(query, "query cannot be null");
        this.query = query.toString();
    }

    public Query(MongoCollection collection) {
        Objects.requireNonNull(collection, "collection cannot be null");
        this.collection = collection;
    }

    public Query setLimit(int limit) {
        findOptions.setLimit(limit);
        return this;
    }

    public Query addSort(String field, FindOrder order) {
        JsonObject sort = findOptions.getSort();
        if (null == sort) {
            sort = new JsonObject();
        }

        sort.put(field, order.getValue());
        return this;
    }

    public Query addCondition(String field, Object value) {
        String newQuery;
        try {
            newQuery = new JsonObject(query).put(field, value).toString();
        } catch (IllegalStateException e) {
            newQuery = new JsonObject(query).put(field, value.toString()).toString();
        }

        query = newQuery;
        return this;
    }

    public Query setFindOne(boolean findOne) {
        this.findOne = findOne;
        return this;
    }

    public boolean isFindOne() {
        return findOne;
    }

    public FindOptions getFindOptions() {
        return findOptions;
    }

    public JsonObject getQuery() {
        return new JsonObject(query);
    }

    public MongoCollection getCollection() {
        return collection;
    }

    @Override
    public String asJsonString() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public String toString() {
        return "Query{" +
                "query='" + query + '\'' +
                ", findOne=" + findOne +
                '}';
    }
}
