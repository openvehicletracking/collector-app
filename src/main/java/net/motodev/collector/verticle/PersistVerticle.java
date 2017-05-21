package net.motodev.collector.verticle;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import net.motodev.collector.helper.MongoHelper;
import net.motodev.core.MessageHandler;
import net.motodev.core.MotodevCollector;
import net.motodev.core.utility.DateUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by yo on 21/05/2017.
 */
public class PersistVerticle extends MotodevAbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistVerticle.class);

    @Override
    public void start() throws Exception {
        LOGGER.info("Starting verticle " + PersistVerticle.class.getName() );
        EventBus eventBus = vertx.eventBus();

        MessageConsumer<String> consumer = eventBus.consumer(MotodevCollector.Constant.PERSIST);
        consumer.handler(messageHandler());
    }

    private Handler<Message<String>> messageHandler() {
        return message -> {
            MessageHandler handler = findHandler(message.body());

            if (handler == null) {
                LOGGER.error("no class definition found for message {}", message.body());
                return;
            }

            net.motodev.core.Message m = handler.handle(message.body());
            if (m.isCommand()) {
                updateCommand(m);
                return;
            }

            JsonObject body = new JsonObject(Json.encode(m));

            MongoClient mongoClient = MongoClient.createNonShared(vertx, config().getJsonObject("database").getJsonObject("mongodb"));
            body.put("datetime", new JsonObject().put("$date", DateUtility.toISODateFormat(m.messageDate())));
            body.put("createdAt", new JsonObject().put("$date", DateUtility.toISODateFormat(new Date())));
            body.put("messageType", m.type());
            body.put("deviceType", m.device());

            mongoClient.insert(MongoHelper.MESSAGES, body, result -> {
                if (result.failed()) {
                    LOGGER.error("An error occurred while saving user message deviceId " + m.deviceId(), result.cause());
                }

                mongoClient.close();
            });
        };
    }

    private void updateCommand(net.motodev.core.Message message) {
        JsonObject query = new JsonObject();
        query.put("deviceId", message.deviceId());
        query.put("requestId", message.requestId());

        JsonObject update = new JsonObject();
        JsonObject $set = new JsonObject();
        JsonObject deviceResponse = new JsonObject();

        if (null != message.extraParameters()) {
            deviceResponse.put("params", new JsonArray(Arrays.asList(message.extraParameters())));
        }

        deviceResponse.put("responseTime", new JsonObject().put("$date", DateUtility.toISODateFormat(message.messageDate())));

        $set.put("response", deviceResponse);
        $set.put("read", true);

        update.put("$set", $set);

        MongoClient mongoClient = MongoClient.createNonShared(vertx, config().getJsonObject("database").getJsonObject("mongodb"));
        mongoClient.updateCollection(MongoHelper.COMMANDS, query, update, result -> mongoClient.close());

    }
}

