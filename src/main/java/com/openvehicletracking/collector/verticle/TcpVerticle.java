package com.openvehicletracking.collector.verticle;


import com.openvehicletracking.collector.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
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
    private final Config config = Config.getInstance();

    @Override
    public void start() throws Exception {
        int port = config.getInt("serverPort", 9001);
        String host = config.getString("serverHost", "0.0.0.0");

        LOGGER.info("Starting verticle {}, on {}:{}", this.getClass().getSimpleName(), host, port);

        NetServerOptions serverOptions = new NetServerOptions();
        serverOptions.setPort(port);
        serverOptions.setHost(host);
        serverOptions.setIdleTimeout(600);
        NetServer server = vertx.createNetServer(serverOptions);
        server.connectHandler(this::connectHandler);
        server.listen();
    }

    private void connectHandler(final NetSocket socket) {
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setWorker(true);
        vertx.deployVerticle(new MessageProcessorVerticle(socket), deploymentOptions, result -> {
            String deploymentId = result.result();
            socket.closeHandler(connectionCloseHandler(socket, deploymentId));
        });
    }

    private Handler<Void> connectionCloseHandler(final NetSocket socket, final String deploymentId) {
        return res -> {
            LOGGER.info("socket closed {}:{}", socket.remoteAddress().host(), socket.remoteAddress().port());
            vertx.undeploy(deploymentId);
        };
    }
}
