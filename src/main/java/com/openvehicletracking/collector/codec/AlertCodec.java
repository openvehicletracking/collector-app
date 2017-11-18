package com.openvehicletracking.collector.codec;

import com.openvehicletracking.core.GsonFactory;
import com.openvehicletracking.core.alert.Alert;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Created by yo on 08/10/2017.
 */
public class AlertCodec implements MessageCodec<Alert, Alert> {

    @Override
    public void encodeToWire(Buffer buffer, Alert alert) {
        buffer.appendInt(alert.asJsonString().getBytes().length);
        buffer.appendString(alert.asJsonString());
    }

    @Override
    public Alert decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        int begin = pos + 4;
        int end = begin + length;
        String toRecord = buffer.getString(begin, end);
        return GsonFactory.getGson().fromJson(toRecord, Alert.class);
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
