package com.openvehicletracking.collector.verticle;

import com.google.gson.Gson;
import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.db.MongoCollection;
import com.openvehicletracking.collector.db.Query;
import com.openvehicletracking.collector.db.Record;
import com.openvehicletracking.collector.db.Result;
import com.openvehicletracking.core.Device;
import com.openvehicletracking.core.DeviceRegistry;
import com.openvehicletracking.core.message.MessageHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by oksuz on 12/09/2017.
 *
 */
public class ParserVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParserVerticle.class);

    private static class DeviceAndHandlerFinder {

        private Device device;
        private MessageHandler handler;

        DeviceAndHandlerFinder(String message) {
            CopyOnWriteArrayList<Device> devices = DeviceRegistry.getInstance().getDevices();

            firstLoop: for (Device device : devices) {
                CopyOnWriteArrayList<MessageHandler> handlers = device.getHandlers();
                for (MessageHandler handler : handlers) {
                    if (handler.pattern().matcher(message).matches()) {
                        this.handler = handler;
                        this.device = device;
                        break firstLoop;
                    }
                }
            }
        }

        Device getDevice() {
            return device;
        }

        MessageHandler getHandler() {
            return handler;
        }
    }

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        MessageConsumer<Buffer> consumer = eventBus.consumer(AppConstants.Events.NEW_RAW_MESSAGE);
        consumer.handler(this::handler);
    }

    private void handler(Message<Buffer> buffer) {
        String rawMessage = buffer.body().toString();
        DeviceAndHandlerFinder deviceAndHandlerFinder = new DeviceAndHandlerFinder(rawMessage);

        if (deviceAndHandlerFinder.getHandler() == null) {
            LOGGER.error("there are no handler registered for this rawMessage: {}", rawMessage);
            return;
        }

        LOGGER.debug("Handler {} found for message {}", deviceAndHandlerFinder.getHandler().getClass().getCanonicalName(), rawMessage);
        com.openvehicletracking.core.message.Message message = deviceAndHandlerFinder.getHandler().handle(rawMessage);

        Record record;
        if (message.isCommand()) {
            JsonObject updateQueryJson = new JsonObject().put("deviceId", message.getDeviceId())
                    .put("requestId", message.getRequestId())
                    .put("read", false);

            JsonObject recordJson = new JsonObject(new Gson().toJson(message)).put("read", true);
            Query updateQuery = new Query(MongoCollection.COMMANDS, updateQueryJson);

            record = new Record(MongoCollection.COMMANDS, recordJson, updateQuery);
            vertx.eventBus().send(AppConstants.Events.COMMAND_UPDATE, record);
            return;
        }

        if (message.isReplyRequired()) {
            JsonObject queryJson = new JsonObject().put("deviceId", message.getDeviceId())
                    .put("read", false);

            Query query = new Query(MongoCollection.COMMANDS, queryJson);
            vertx.eventBus().<Result<List<JsonObject>>>send(AppConstants.Events.NEW_QUERY, query, result -> {
                if (result.succeeded()) {
                    Result<List<JsonObject>> commandsResult = result.result().body();
                    List<String> commands = new ArrayList<>();
                    commandsResult.getResult().forEach(json -> commands.add(json.getString("command")));
                    buffer.reply(new Result<>(commands, false, null));
                }
            });
        }


        if (message.getClass() == deviceAndHandlerFinder.getDevice().getLocationType()) {
            record = new Record(MongoCollection.MESSAGES, new JsonObject(new Gson().toJson(message)));
            vertx.eventBus().send(AppConstants.Events.PERSIST, record);
        }
    }

}
