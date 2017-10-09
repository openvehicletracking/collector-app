package com.openvehicletracking.collector.verticle;

import com.google.gson.Gson;
import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.cache.DeviceStateCache;
import com.openvehicletracking.collector.db.MongoCollection;
import com.openvehicletracking.collector.db.Query;
import com.openvehicletracking.collector.db.Record;
import com.openvehicletracking.core.Device;
import com.openvehicletracking.core.DeviceRegistry;
import com.openvehicletracking.core.DeviceState;
import com.openvehicletracking.core.alarm.Alarm;
import com.openvehicletracking.core.exception.UnsupportedMessageTypeException;
import com.openvehicletracking.core.message.MessageHandler;
import com.openvehicletracking.core.message.Reply;
import com.openvehicletracking.core.message.exception.UnsupportedReplyTypeException;
import com.openvehicletracking.core.message.impl.StringCommandMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
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
public class MessageProcessorVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessorVerticle.class);

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
        Gson gson = new Gson();
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
            vertx.eventBus().send(AppConstants.Events.UPDATE, record);
            return;
        }

        if (message.isReplyRequired()) {
            JsonObject queryJson = new JsonObject().put("deviceId", message.getDeviceId())
                    .put("read", false);

            Query query = new Query(MongoCollection.COMMANDS, queryJson);
            vertx.eventBus().<JsonArray>send(AppConstants.Events.NEW_QUERY, query, result -> {
                if (result.failed()) { return; }

                if (result.result().body() == null) {
                    LOGGER.error("result body null");
                    return;
                }

                List<StringCommandMessage> commands = new ArrayList<>();
                result.result().body().stream().forEach(json -> commands.add(gson.fromJson(json.toString(), StringCommandMessage.class)));

                Reply<String> replies = null;
                try {
                    replies = deviceAndHandlerFinder.getDevice().replyMessage(message, commands);
                } catch (UnsupportedReplyTypeException ignored) {
                    return;
                }
                buffer.reply(new JsonArray(replies.get()));
            });
        }

        if (message.getClass() == deviceAndHandlerFinder.getDevice().getLocationType()) {
            record = new Record(MongoCollection.MESSAGES, new JsonObject(new Gson().toJson(message)));
            vertx.eventBus().send(AppConstants.Events.PERSIST, record);
            try {
                DeviceState state = deviceAndHandlerFinder.getDevice().createStateFromMessage(message);
                if (state != null) {
                    DeviceStateCache.getInstance().put(state);
                }
            } catch (UnsupportedMessageTypeException e) {
                LOGGER.error("UnsupportedMessageTypeException", e);
            }

            Alarm alarm = deviceAndHandlerFinder.getDevice().generateAlarmFromMessage(message);
            if (alarm != null) {
                vertx.eventBus().send(AppConstants.Events.ALARM, alarm);
            }
        }
    }
}
