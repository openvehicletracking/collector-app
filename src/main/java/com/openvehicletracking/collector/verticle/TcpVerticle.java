package com.openvehicletracking.collector.verticle;


import com.openvehicletracking.collector.Config;
import com.openvehicletracking.collector.connection.ActiveDeviceConnection;
import com.openvehicletracking.collector.connection.NetSocketConnectionHolder;
import com.openvehicletracking.collector.SessionManager;
import com.openvehicletracking.collector.processor.MessageProcessor;
import com.openvehicletracking.collector.processor.impl.MessageProcessorImpl;
import com.openvehicletracking.core.protocol.Message;
import com.openvehicletracking.core.protocol.ProtocolChain;
import com.openvehicletracking.core.protocol.impl.ProtocolChainImpl;
import com.openvehicletracking.protocols.xtakip.XTakipProtocol;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
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
    private final ProtocolChain protocolChain = new ProtocolChainImpl();
    private MessageProcessor messageProcessor;

    private final Config config = Config.getInstance();

    @Override
    public void start() throws Exception {
        protocolChain.add(new XTakipProtocol());

        messageProcessor = new MessageProcessorImpl(vertx.eventBus());

        int port = config.getInt("serverPort", 9001);
        String host = config.getString("serverHost", "0.0.0.0");

        LOGGER.info("Starting verticle {}, on {}:{}", this.getClass().getSimpleName(), host, port);

        NetServerOptions serverOptions = new NetServerOptions();
        serverOptions.setPort(port);
        serverOptions.setHost(host);
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
        return buffer -> {
            Message deviceMessage = protocolChain.handle(buffer.getBytes(), new NetSocketConnectionHolder(socket));
            if (deviceMessage == null) {
                return;
            }

            SessionManager.getInstance().updateSession(new ActiveDeviceConnection(socket.remoteAddress().host(), deviceMessage.getDevice()));

            new Thread(() -> {
                try {
                    messageProcessor.process(deviceMessage);
                } catch (Exception e) {
                    LOGGER.error("exception on processing message", e);
                }
            }).start();
        };
    }
}
