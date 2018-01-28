package com.openvehicletracking.collector;

import com.openvehicletracking.core.ConnectionHolder;
import com.openvehicletracking.core.Reply;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

public class NetSocketConnectionHolder implements ConnectionHolder<NetSocket> {

    private final NetSocket socket;

    public NetSocketConnectionHolder(NetSocket socket) {
        this.socket = socket;
    }

    @Override
    public void write(Reply reply) {
        socket.write(Buffer.buffer(reply.getData()));
    }

    @Override
    public NetSocket getConnection() {
        return socket;
    }
}
