package com.openvehicletracking.collector.codec;

import com.google.gson.Gson;
import com.openvehicletracking.collector.db.Record;
import com.openvehicletracking.core.GsonFactory;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Created by oksuz on 23/09/2017.
 *
 */
public class RecordCodec implements MessageCodec<Record, Record> {

    private final Gson gson = GsonFactory.getGson();

    @Override
    public void encodeToWire(Buffer buffer, Record record) {
        String rec = gson.toJson(record);
        buffer.appendInt(rec.getBytes().length);
        buffer.appendString(rec);
    }

    @Override
    public Record decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        int begin = pos + 4;
        int end = begin + length;
        String toRecord = buffer.getString(begin, end);
        return gson.fromJson(toRecord, Record.class);
    }

    @Override
    public Record transform(Record record) {
        return record;
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
