package com.openvehicletracking.collector.db;

import io.vertx.core.json.JsonObject;

import java.io.Serializable;

/**
 * Created by yo on 08/10/2017.
 */
public class UpdateResult implements Serializable {

    private long docMatched;
    private long docModified;
    private JsonObject docUpsertedId;

    public UpdateResult(long docMatched, long docModified, JsonObject docUpsertedId) {
        this.docMatched = docMatched;
        this.docModified = docModified;
        this.docUpsertedId = docUpsertedId;
    }

    public long getDocMatched() {
        return docMatched;
    }

    public long getDocModified() {
        return docModified;
    }

    public JsonObject getDocUpsertedId() {
        return docUpsertedId;
    }
}
