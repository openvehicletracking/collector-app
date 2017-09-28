package com.openvehicletracking.collector.codec;

import com.google.gson.Gson;
import com.openvehicletracking.collector.db.Query;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Created by oksuz on 23/09/2017.
 *
 */
public class QueryCodec implements MessageCodec<Query, Query> {

    private final Gson gson = new Gson();

    @Override
    public void encodeToWire(Buffer buffer, Query record) {
        String rec = gson.toJson(record);

        buffer.appendInt(rec.getBytes().length);
        buffer.appendString(rec);
    }

    @Override
    public Query decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        int begin = pos + 4;
        int end = begin + length;
        String toRecord = buffer.getString(begin, end);
        return gson.fromJson(toRecord, Query.class);
    }

    @Override
    public Query transform(Query query) {
        return query;
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
