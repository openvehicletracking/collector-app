package com.openvehicletracking.collector.codec;

import com.google.gson.Gson;
import com.openvehicletracking.core.alarm.Alarm;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Created by yo on 08/10/2017.
 */
public class AlarmCodec implements MessageCodec<Alarm, Alarm> {

    private final Gson gson = new Gson();

    @Override
    public void encodeToWire(Buffer buffer, Alarm alarm) {
        String rec = gson.toJson(alarm);
        buffer.appendInt(rec.getBytes().length);
        buffer.appendString(rec);
    }

    @Override
    public Alarm decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        int begin = pos + 4;
        int end = begin + length;
        String toRecord = buffer.getString(begin, end);
        return gson.fromJson(toRecord, Alarm.class);
    }

    @Override
    public Alarm transform(Alarm alarm) {
        return alarm;
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
