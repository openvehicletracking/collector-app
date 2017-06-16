package net.motodev.collector.verticle;

import com.google.gson.Gson;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import net.motodev.core.Motodev;
import net.motodev.core.MotodevAbstractVerticle;
import net.motodev.core.message.Message;
import net.motodev.core.message.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by oksuz on 28/01/2017.
 */
public class NewMessageVerticle extends MotodevAbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewMessageVerticle.class);

    @Override
    public void start() throws Exception {
        LOGGER.info("Starting verticle " + NewMessageVerticle.class.getName() );
        EventBus eventBus = vertx.eventBus();

        MessageConsumer<Buffer> consumer = eventBus.consumer(Motodev.Constant.NEW_MESSAGE);
        consumer.handler(messageHandler(new Gson()));
    }

    private Handler<io.vertx.core.eventbus.Message<Buffer>> messageHandler(Gson gson) {
        return bufferMessage -> {
            Buffer buffer = bufferMessage.body();
            String message = buffer.toString();

            LOGGER.debug("new message handled from " + this.toString());
            LOGGER.debug("new incoming message `{}`", message);

            MessageHandler handler = findHandler(message);

            if (handler == null) {
                LOGGER.info("There are no handler available for this message `{}`", message);
                return;
            }

            Message m = handler.handle(message);
            JsonObject messageToSend = new JsonObject(Json.encode(m));
            if (!messageToSend.containsKey("deviceId")) {
                messageToSend.put("deviceId", m.deviceId());
            }

            vertx.eventBus().send(Motodev.Constant.DEVICE_COMMAND, messageToSend, replyHandler(bufferMessage));
            vertx.eventBus().send(Motodev.Constant.PERSIST, message, replyHandler(bufferMessage));
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
