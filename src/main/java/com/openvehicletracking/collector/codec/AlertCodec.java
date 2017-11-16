package com.openvehicletracking.collector.codec;

import com.google.gson.Gson;
import com.openvehicletracking.core.GsonFactory;
import com.openvehicletracking.core.alert.Alert;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Created by yo on 08/10/2017.
 */
public class AlertCodec implements MessageCodec<Alert, Alert> {

    private final Gson gson = GsonFactory.getGson();

    @Override
    public void encodeToWire(Buffer buffer, Alert alarm) {
        String rec = gson.toJson(alarm);
        buffer.appendInt(rec.getBytes().length);
        buffer.appendString(rec);
    }

    @Override
    public Alert decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        int begin = pos + 4;
        int end = begin + length;
        String toRecord = buffer.getString(begin, end);
        return gson.fromJson(toRecord, Alert.class);
    }

    @Override
    public Alert transform(Alert alarm) {
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
