package com.openvehicletracking.collector.codec;

import com.google.gson.Gson;
import com.openvehicletracking.collector.db.Result;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Created by yo on 28/09/2017.
 */
public class ResultCodec implements MessageCodec<Result, Result> {

    private final Gson gson = new Gson();

    @Override
    public void encodeToWire(Buffer buffer, Result result) {
        String rec = gson.toJson(result);
        buffer.appendInt(rec.getBytes().length);
        buffer.appendString(rec);
    }

    @Override
    public Result decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        int begin = pos + 4;
        int end = begin + length;
        String toRecord = buffer.getString(begin, end);
        return gson.fromJson(toRecord, Result.class);
    }

    @Override
    public Result transform(Result result) {
        return result;
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
