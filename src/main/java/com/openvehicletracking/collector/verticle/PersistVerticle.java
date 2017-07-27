package com.openvehicletracking.collector.verticle;

import com.google.gson.Gson;
import com.openvehicletracking.core.Device;
import com.openvehicletracking.core.OpenVehicleTracker;
import com.openvehicletracking.core.TrackerAbstractVerticle;
import com.openvehicletracking.core.db.*;
import com.openvehicletracking.core.exception.MandatoryFieldException;
import com.openvehicletracking.core.message.MessageHandler;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by oksuz on 21/05/2017.
 *
 */
public class PersistVerticle extends TrackerAbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistVerticle.class);
    private static final OpenVehicleTracker openVehicleTracker = OpenVehicleTracker.getInstance();
    private static final DBClientFactory dbClientFactory = openVehicleTracker.getDbClientFactory();
    private AlarmDAO alarmDAO;
    private DeviceDAO deviceDAO;
    private MessagesDAO messagesDAO;
    private CommandDAO commandDAO;

    @Override
    public void start() throws Exception {
        LOGGER.info("Starting verticle " + PersistVerticle.class.getName() );
        EventBus eventBus = vertx.eventBus();

        alarmDAO = new AlarmDAO(dbClientFactory);
        messagesDAO = new MessagesDAO(dbClientFactory);

        MessageConsumer<String> consumer = eventBus.consumer(OpenVehicleTracker.Constant.PERSIST);
        consumer.handler(messageHandler(new Gson()));
    }

    private Handler<Message<String>> messageHandler(Gson gson) {
        return message -> {
            MessageHandler handler = findHandler(message.body());
            com.openvehicletracking.core.message.Message m = handler.handle(message.body());
            commandDAO = new CommandDAO(dbClientFactory, m.getDeviceId());

            if (m.isCommand()) {
                LOGGER.info("Message is command, updating command: {}", m);
                try {
                    commandDAO.update(m);
                } catch (MandatoryFieldException e) {
                    LOGGER.error("error while updating command", e);
                }
                return;
            }

            Device device = openVehicleTracker.getDeviceRegistry().findDevice(m.getDevice());
            if (device == null) {
                LOGGER.error("There no device found for message {}, messageDevice: {}", m, m.getDevice());
                return;
            }

            LOGGER.info("found device [{}] for message {}", device.getName(), message.body());

            deviceDAO = new DeviceDAO(dbClientFactory, m.getDeviceId(), alarmDAO);
            device.generateAlarmFromMessage(m, deviceDAO, alarm -> {
                if (null != alarm) {
                    alarmDAO.save(alarm);
                    openVehicleTracker.getVertx().eventBus().send(OpenVehicleTracker.Constant.ALARM, gson.toJson(alarm));
                }
            });

            device.updateMeta(m, deviceDAO);
            messagesDAO.save(m);
        };
    }
}

