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
        String rawMessage = buffer.body().toString();
        DeviceAndHandlerFinder deviceAndHandlerFinder = new DeviceAndHandlerFinder(rawMessage);

        if (deviceAndHandlerFinder.getHandler() == null) {
            LOGGER.error("there are no handler registered for this rawMessage: {}", rawMessage);
            return;
        }

        LOGGER.debug("Handler {} found for message {}", deviceAndHandlerFinder.getHandler().getClass().getCanonicalName(), rawMessage);
        com.openvehicletracking.core.message.Message message = deviceAndHandlerFinder.getHandler().handle(rawMessage);


        try {
            createAlertIfRequired(message, deviceAndHandlerFinder);
        } catch (Exception e) {
            LOGGER.error("An error occurred while creating alert", e);
        }

        try {
            updateIfCommand(message);
        } catch (Exception e) {
            LOGGER.error("An error occurred while updating command", e);
        }

        try {
            replyIfRequired(message, buffer, deviceAndHandlerFinder);
        } catch (Exception e) {
            LOGGER.error("An error occurred while replying", e);
        }

        try {
            persistIfLocationMessage(message, deviceAndHandlerFinder);
        } catch (Exception e) {
            LOGGER.error("An error occurred while persisting message", e);
        }

        try {
            createStateFromMessage(message, deviceAndHandlerFinder);
        } catch (Exception e) {
            LOGGER.error("An error occurred while creating state", e);
        }

    }

    private void createStateFromMessage(com.openvehicletracking.core.message.Message message, DeviceAndHandlerFinder deviceAndHandlerFinder) {
        try {
            DeviceState state = deviceAndHandlerFinder.getDevice().createStateFromMessage(message);
            if (state != null) {
                DeviceStateCache.getInstance().put(state);
            }
        } catch (UnsupportedMessageTypeException e) {
            LOGGER.debug("UnsupportedMessageTypeException", e);
        }
    }

    private void persistIfLocationMessage(com.openvehicletracking.core.message.Message message, DeviceAndHandlerFinder deviceAndHandlerFinder) {
        if (message.getClass() == deviceAndHandlerFinder.getDevice().getLocationType()) {
            Record record = new Record(MongoCollection.MESSAGES, new JsonObject(new Gson().toJson(message)));
            vertx.eventBus().send(AppConstants.Events.PERSIST, record);
        }
    }

    private void createAlertIfRequired(com.openvehicletracking.core.message.Message message, DeviceAndHandlerFinder deviceAndHandlerFinder) {
        Alarm alarm = deviceAndHandlerFinder.getDevice().generateAlarmFromMessage(message);
        if (alarm != null) {
            LOGGER.debug("alert generated from message {}", alarm);
            vertx.eventBus().send(AppConstants.Events.ALARM, alarm);
        }
    }

    private void replyIfRequired(com.openvehicletracking.core.message.Message message, Message<Buffer> buffer, DeviceAndHandlerFinder deviceAndHandlerFinder) {
        if (!message.isReplyRequired()) {
            LOGGER.debug("no reply required {}", message);
            return;
        }

        JsonObject queryJson = new JsonObject().put("deviceId", message.getDeviceId())
                .put("isRead", false);

        Query query = new Query(MongoCollection.COMMANDS, queryJson);
        vertx.eventBus().<JsonArray>send(AppConstants.Events.NEW_QUERY, query, result -> {
            if (result.failed()) { return; }

            if (result.result().body() == null || result.result().body().size() == 0) {
                LOGGER.debug("reply result null or empty");
                return;
            }

            List<StringCommandMessage> commands = new ArrayList<>();
            result.result().body().stream().forEach(json -> commands.add(new Gson().fromJson(json.toString(), StringCommandMessage.class)));

            Reply<String> replies = null;
            try {
                replies = deviceAndHandlerFinder.getDevice().replyMessage(message, commands);
            } catch (UnsupportedReplyTypeException ignored) {
                return;
            }

            if (replies != null) {
                buffer.reply(new JsonArray(replies.get()));
            }
        });
    }

    private void updateIfCommand(com.openvehicletracking.core.message.Message message) {
        if (!message.isCommand()) {
            LOGGER.debug("message is not command {}", message);
            return;
        }

        JsonObject updateQueryJson = new JsonObject().put("deviceId", message.getDeviceId())
                .put("requestId", message.getRequestId().get())
                .put("isRead", false);

        JsonObject recordJson = new JsonObject(new Gson().toJson(message)).put("isRead", true);
        Query updateQuery = new Query(MongoCollection.COMMANDS, updateQueryJson);

        Record record = new Record(MongoCollection.COMMANDS, recordJson, updateQuery);
        vertx.eventBus().send(AppConstants.Events.UPDATE, record);
    }
}
