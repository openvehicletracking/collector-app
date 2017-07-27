package com.openvehicletracking.collector.verticle;

import com.openvehicletracking.core.OpenVehicleTracker;
import com.openvehicletracking.core.TrackerAbstractVerticle;
import com.openvehicletracking.core.message.MessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by oksuz on 28/01/2017.
 */
public class NewMessageVerticle extends TrackerAbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewMessageVerticle.class);

    @Override
    public void start() throws Exception {
        LOGGER.info("Starting verticle " + NewMessageVerticle.class.getName() );
        EventBus eventBus = vertx.eventBus();

        MessageConsumer<Buffer> consumer = eventBus.consumer(OpenVehicleTracker.Constant.NEW_MESSAGE);
        consumer.handler(messageHandler());
    }

    private Handler<io.vertx.core.eventbus.Message<Buffer>> messageHandler() {
        return bufferMessage -> {
            Buffer buffer = bufferMessage.body();
            String message = buffer.toString();
            MessageHandler handler = findHandler(message);
            if (handler == null) {
                LOGGER.info("There are no handler available for this message `{}`", message);
                return;
            }

            vertx.eventBus().send(OpenVehicleTracker.Constant.DEVICE_COMMAND, message, replyHandler(bufferMessage));
            vertx.eventBus().send(OpenVehicleTracker.Constant.PERSIST, message, replyHandler(bufferMessage));
        };
    }

    private Handler<AsyncResult<io.vertx.core.eventbus.Message<Buffer>>> replyHandler(io.vertx.core.eventbus.Message msg) {
        return reply -> {
            if (reply.succeeded()) {
                msg.reply(reply.result().body());
            }
        };
    }

}
