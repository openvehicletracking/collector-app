package com.openmts.collector.verticle;

import com.google.gson.Gson;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import com.openmts.core.Device;
import com.openmts.core.Motodev;
import com.openmts.core.MotodevAbstractVerticle;
import com.openmts.core.db.DeviceQueryHelper;
import com.openmts.core.message.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by oksuz on 21/05/2017.
 *
 */
public class PersistVerticle extends MotodevAbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistVerticle.class);
    private static final Motodev motodev = Motodev.getInstance();

    @Override
    public void start() throws Exception {
        LOGGER.info("Starting verticle " + PersistVerticle.class.getName() );
        EventBus eventBus = vertx.eventBus();

        MessageConsumer<String> consumer = eventBus.consumer(Motodev.Constant.PERSIST);
        consumer.handler(messageHandler(new Gson()));
    }

    private Handler<Message<String>> messageHandler(Gson gson) {
        return message -> {
            MessageHandler handler = findHandler(message.body());

            if (handler == null) {
                LOGGER.error("no class definition found for message {}", message.body());
                return;
            }

            com.openmts.core.message.Message m = handler.handle(message.body());

            if (m.isCommand()) {
                LOGGER.info("Message is command, updating command: {}", m);
                motodev.getPersistor().updateCommand(m);
                return;
            }

            Device device = motodev.getDeviceRegistry().findDevice(m.device());
            LOGGER.info("found device [{}] for message {}", device.name(), message.body());

            DeviceQueryHelper deviceQueryExecutor = new DeviceQueryHelper(m.deviceId(), motodev.getPersistor());

            device.createAlarmIfRequired(m, deviceQueryExecutor, alarm -> {
                if (null != alarm) {
                    motodev.getPersistor().createAlarm(alarm);
                    motodev.getVertx().eventBus().send(Motodev.Constant.ALARM, gson.toJson(alarm));
                }
            });

            device.updateMeta(m, deviceQueryExecutor);
            motodev.getPersistor().saveMessage(m);
        };
    }
}

