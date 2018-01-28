package com.openvehicletracking.collector.verticle;

import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.database.MongoCollection;
import com.openvehicletracking.collector.database.Record;
import com.openvehicletracking.core.protocol.Message;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MessageProcessorVerticle extends AbstractMessageProcessorVerticle {

    private static Logger LOGGER = LoggerFactory.getLogger(TcpVerticle.class);

    public MessageProcessorVerticle(NetSocket socket) {
        super(socket);
    }

    @Override
    public void handler(Buffer buffer) {
        Message deviceMessage = getMessage(buffer);
        if (deviceMessage == null) {
            return;
        }

        JsonObject message = createJsonMessage(deviceMessage);
        if (message != null) {
            vertx.eventBus().<JsonObject>send(AppConstants.Events.PERSIST, new Record(MongoCollection.MESSAGES, message), result -> {
                LOGGER.info(result.result().body().encodePrettily());
            });
        }

    }


}
