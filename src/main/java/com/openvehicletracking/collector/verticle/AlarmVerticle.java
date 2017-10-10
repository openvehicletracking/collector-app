package com.openvehicletracking.collector.verticle;

import com.google.gson.Gson;
import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.db.MongoCollection;
import com.openvehicletracking.collector.db.Record;
import com.openvehicletracking.core.alarm.Alarm;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by oksuz on 08/10/2017.
 *
 */
public class AlarmVerticle extends AbstractVerticle {

    private static Logger LOGGER = LoggerFactory.getLogger(AlarmVerticle.class);

    @Override
    public void start() throws Exception {
        LOGGER.info("Starting verticle AlarmVerticle");

        MessageConsumer<Alarm> alarmMessageConsumer = vertx.eventBus().consumer(AppConstants.Events.ALARM);
        alarmMessageConsumer.handler(this::alarmHandler);
    }

    private void alarmHandler(Message<Alarm> alarmMessage) {
        Record record = new Record(MongoCollection.ALARMS, new JsonObject(new Gson().toJson(alarmMessage.body())));
        vertx.eventBus().send(AppConstants.Events.PERSIST, record);
    }
}