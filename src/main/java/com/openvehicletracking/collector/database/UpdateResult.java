package com.openvehicletracking.collector.database;


import com.openvehicletracking.core.json.GsonFactory;
import com.openvehicletracking.core.json.JsonSerializeable;
import io.vertx.core.json.JsonObject;

/**
 * Created by oksuz on 08/10/2017.
 *
 */
public class UpdateResult implements JsonSerializeable {

    private long docMatched;
    private long docModified;
    private String docUpsertedId;

    public UpdateResult(long docMatched, long docModified, JsonObject docUpsertedId) {
        this.docMatched = docMatched;
        this.docModified = docModified;
        this.docUpsertedId = (docUpsertedId != null) ? docUpsertedId.toString() : "{}";
    }

    public long getDocMatched() {
        return docMatched;
    }

    public long getDocModified() {
        return docModified;
    }

    public JsonObject getDocUpsertedId() {
        return new JsonObject(docUpsertedId);
    }

    @Override
    public String asJson() {
        return GsonFactory.getGson().toJson(this);
    }
}
