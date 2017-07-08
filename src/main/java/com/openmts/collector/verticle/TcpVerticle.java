package com.openmts.collector.verticle;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import com.openmts.core.Motodev;
import com.openmts.core.MotodevAbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by oksuz on 28/01/2017.
 */
public class TcpVerticle extends MotodevAbstractVerticle {

    private static Logger LOGGER = LoggerFactory.getLogger(TcpVerticle.class);

    private NetServerOptions serverOptions = new NetServerOptions();
    @Override
    public void start() throws Exception {
        serverOptions.setPort(config().getInteger("collectorPort", 9998));
        serverOptions.setHost(config().getString("collectorHost", "0.0.0.0"));

        LOGGER.info("Starting verticle {}, on port: {}", TcpVerticle.class.getSimpleName(), config().getInteger("collectorPort", 9998));
        NetServer server = vertx.createNetServer(serverOptions);
        server.connectHandler(this::connectHandler);
        server.listen();
    }

    private void connectHandler(NetSocket socket) {
        LOGGER.debug("new incoming connection {} handled on instance {}", socket.toString(), this.toString());
        LOGGER.info("new incoming connection remoteaddr: {}:{}", socket.remoteAddress().host(), socket.remoteAddress().port());
        socket.handler(messageHandler(socket));
        socket.closeHandler(connectionCloseHandler(socket));
    }


    private Handler<Void> connectionCloseHandler(NetSocket socket) {
        return res -> LOGGER.info("socket closed, remoteaddr: {}:{}", socket.remoteAddress().host(), socket.remoteAddress().port());
    }

    private Handler<Buffer> messageHandler(NetSocket socket) {
        return buffer -> vertx.eventBus().send(Motodev.Constant.NEW_MESSAGE, buffer, replyHandler(socket));
    }

    private Handler<AsyncResult<Message<JsonArray>>> replyHandler(NetSocket socket) {
        return reply -> {
            if (reply.succeeded()) {
                try {
                    JsonArray messages = reply.result().body();
                    messages.forEach(message -> socket.write(message.toString()));
                } catch (Exception e) {
                    LOGGER.error("Error : " + e.getMessage(), e);
                }
            }
        };
    }
}
