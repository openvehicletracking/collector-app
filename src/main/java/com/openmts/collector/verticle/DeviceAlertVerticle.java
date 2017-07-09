package com.openmts.collector.verticle;

import com.google.gson.Gson;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import com.openmts.core.Motodev;
import com.openmts.core.MotodevAbstractVerticle;
import com.openmts.core.alarm.Alarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by oksuz on 16/06/2017.
 *
 */
public class DeviceAlertVerticle extends MotodevAbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceAlertVerticle.class);
    private static final Motodev motodev = Motodev.getInstance();

    @Override
    public void start() throws Exception {
        LOGGER.info("Starting verticle {}", DeviceAlertVerticle.class.getSimpleName());
        EventBus eventBus = vertx.eventBus();

        MessageConsumer<String> consumer = eventBus.consumer(Motodev.Constant.ALARM);
        consumer.handler(messageHandler(new Gson()));
    }

    private Handler<Message<String>> messageHandler(Gson gson) {
        return message -> {
            Alarm alarm = gson.fromJson(message.body(), Alarm.class);
            LOGGER.info("alarm fired {}", alarm);
        };
    }
}
