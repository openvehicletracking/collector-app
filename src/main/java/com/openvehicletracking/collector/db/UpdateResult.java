package com.openvehicletracking.collector.db;

import com.openvehicletracking.core.GsonFactory;
import com.openvehicletracking.core.JsonDeserializeable;
import com.openvehicletracking.core.JsonSerializeable;
import io.vertx.core.json.JsonObject;

/**
 * Created by oksuz on 08/10/2017.
 *
 */
public class UpdateResult implements JsonSerializeable, JsonDeserializeable<UpdateResult> {

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
    public UpdateResult fromJsonString(String json) {
        return GsonFactory.getGson().fromJson(json, this.getClass());
    }

    @Override
    public String asJsonString() {
        return GsonFactory.getGson().toJson(this);
    }
}
