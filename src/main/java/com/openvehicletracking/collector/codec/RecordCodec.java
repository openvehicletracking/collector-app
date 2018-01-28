package com.openvehicletracking.collector.codec;


import com.openvehicletracking.collector.database.Record;
import io.netty.util.CharsetUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Created by oksuz on 23/09/2017.
 *
 */
public class RecordCodec implements MessageCodec<Record, Record> {

    @Override
    public void encodeToWire(Buffer buffer, Record record) {
        String strJson = record.asJson();
        byte[] encoded = strJson.getBytes(CharsetUtil.UTF_8);
        buffer.appendInt(encoded.length);
        Buffer buff = Buffer.buffer(encoded);
        buffer.appendBuffer(buff);
    }

    @Override
    public Record decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        pos += 4;
        byte[] encoded = buffer.getBytes(pos, pos + length);
        String str = new String(encoded, CharsetUtil.UTF_8);
        return Record.fromJson(str);
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