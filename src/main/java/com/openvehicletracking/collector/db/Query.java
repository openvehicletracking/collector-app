package com.openvehicletracking.collector.db;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by oksuz on 23/09/2017.
 *
 */
public class Query implements Serializable {

    private FindOptions findOptions = new FindOptions();
    private String query;
    private FindOrder findOrder = FindOrder.ASC;
    private MongoCollection collection;
    private boolean findOne = false;

    public Query(MongoCollection collection, JsonObject query) {
        Objects.requireNonNull(collection, "collection cannot be null");
        Objects.requireNonNull(query, "query cannot be null");
        this.collection = collection;
        this.query = query.toString();
    }

    public Query(MongoCollection collection, JsonObject query, FindOptions findOptions) {
        this(collection, query);
        this.findOptions = findOptions;
    }

    public Query(MongoCollection collection, JsonObject query, FindOptions findOptions, FindOrder findOrder) {
        this(collection, query, findOptions);
        this.findOrder = findOrder;
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

    public FindOrder getFindOrder() {
        return findOrder;
    }

    public MongoCollection getCollection() {
        return collection;
    }

    @Override
    public String toString() {
        return "Query{" +
                "query='" + query + '\'' +
                ", findOne=" + findOne +
                '}';
    }
}
