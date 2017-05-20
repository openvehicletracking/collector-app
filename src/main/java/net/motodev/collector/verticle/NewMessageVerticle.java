package net.motodev.collector.verticle;

import com.google.gson.Gson;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import net.motodev.core.Device;
import net.motodev.core.Message;
import net.motodev.core.MessageHandler;
import net.motodev.core.MotodevCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;

/**
 * Created by oksuz on 28/01/2017.
 */
public class NewMessageVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewMessageVerticle.class);

    @Override
    public void start() throws Exception {
        LOGGER.info("Starting verticle " + NewMessageVerticle.class.getName() );
        EventBus eventBus = vertx.eventBus();

        MessageConsumer<Buffer> consumer = eventBus.consumer(MotodevCollector.Constant.NEW_MESSAGE);
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

            MongoClient mongoClient = MongoClient.createNonShared(vertx, config().getJsonObject("database").getJsonObject("mongodb"));

            Message m = handler.handle(message);
            m.save(mongoClient, "messages", result -> mongoClient.close());

            vertx.eventBus().send(m.subject(), new JsonObject(Json.encode(m)), replyHandler(bufferMessage));
        };
    }

    private MessageHandler findHandler(String message) {
        List<Device> devices = MotodevCollector.getInstance().deviceRegistry().getDevices();
        for (Device d : devices) {
            Vector<MessageHandler> handlers = d.handlers();
            for (MessageHandler handler : handlers) {
                Matcher matcher = handler.pattern().matcher(message);
                if (matcher.matches()) {
                    return handler;
                }
            }
        }

        return null;
    }

    private Handler<AsyncResult<io.vertx.core.eventbus.Message<Buffer>>> replyHandler(io.vertx.core.eventbus.Message msg) {
        return reply -> {
            if (reply.succeeded()) {
                msg.reply(reply.result().body());
            }
        };
    }

}
