package com.openvehicletracking.collector.codec;


import com.openvehicletracking.collector.notification.SendResult;
import com.openvehicletracking.collector.notification.sms.SmsSendResult;
import com.openvehicletracking.core.GsonFactory;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class SmsSendResultCodec implements MessageCodec<SendResult, SmsSendResult> {

    @Override
    public void encodeToWire(Buffer buffer, SendResult sendResult) {
        buffer.appendInt(sendResult.asJsonString().getBytes().length);
        buffer.appendString(sendResult.asJsonString());
    }

    @Override
    public SmsSendResult decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        int begin = pos + 4;
        int end = begin + length;
        String toRecord = buffer.getString(begin, end);
        return GsonFactory.getGson().fromJson(toRecord, SmsSendResult.class);
    }

    @Override
    public SmsSendResult transform(SendResult sendResult) {
        return (SmsSendResult) sendResult;
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
