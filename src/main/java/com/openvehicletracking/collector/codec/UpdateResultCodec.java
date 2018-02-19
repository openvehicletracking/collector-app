package com.openvehicletracking.collector.codec;

import com.openvehicletracking.collector.db.UpdateResult;
import com.openvehicletracking.core.GsonFactory;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Created by yo on 08/10/2017.
 */
public class UpdateResultCodec implements MessageCodec<UpdateResult, UpdateResult> {

    @Override
    public void encodeToWire(Buffer buffer, UpdateResult updateResult) {
        buffer.appendInt(updateResult.asJsonString().getBytes().length);
        buffer.appendString(updateResult.asJsonString());
    }

    @Override
    public UpdateResult decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        int begin = pos + 4;
        int end = begin + length;
        String toRecord = buffer.getString(begin, end);
        return GsonFactory.getGson().fromJson(toRecord, UpdateResult.class);
    }

    @Override
    public UpdateResult transform(UpdateResult updateResult) {
        return updateResult;
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
