package com.openvehicletracking.collector.verticle;

import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.db.Result;
import com.openvehicletracking.core.message.Reply;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by oksuz on 28/01/2017.
 *
 */
public class TcpVerticle extends AbstractVerticle {

    private static Logger LOGGER = LoggerFactory.getLogger(TcpVerticle.class);

    @Override
    public void start() throws Exception {
        int port = config().getInteger("collectorPort", 9998);
        String host = config().getString("collectorHost", "0.0.0.0");

        NetServerOptions serverOptions = new NetServerOptions();
        serverOptions.setPort(port);
        serverOptions.setHost(host);

        LOGGER.info("Starting verticle {}, on {}:{}", this.getClass().getSimpleName(), host, port);
        NetServer server = vertx.createNetServer(serverOptions);
        server.connectHandler(this::connectHandler);
        server.listen();
    }

    private void connectHandler(NetSocket socket) {
        LOGGER.info("new incoming connection from {}:{}", socket.remoteAddress().host(), socket.remoteAddress().port());
        socket.handler(messageHandler(socket));
        socket.closeHandler(connectionCloseHandler(socket));
    }


    private Handler<Void> connectionCloseHandler(NetSocket socket) {
        return res -> LOGGER.info("socket closed {}:{}", socket.remoteAddress().host(), socket.remoteAddress().port());
    }

    private Handler<Buffer> messageHandler(NetSocket socket) {
        return buffer -> vertx.eventBus().send(AppConstants.Events.NEW_RAW_MESSAGE, buffer, replyHandler(socket));
    }

    private Handler<AsyncResult<Message<Result<Reply<String>>>>> replyHandler(NetSocket socket) {
        return reply -> {
            if (reply.failed()) {
                LOGGER.error("reply failed", reply.cause());
                return;
            }

            try {
                reply.result().body().getResult().get().forEach(socket::write);
            } catch (Exception e) {
                LOGGER.error("error while writing msg to socket : " + e.getMessage(), e);
            }


        };
    }
}
