package com.openvehicletracking.collector.verticle;

import com.openvehicletracking.core.Device;
import com.openvehicletracking.core.OpenVehicleTracker;
import com.openvehicletracking.core.TrackerAbstractVerticle;
import com.openvehicletracking.core.db.CommandDAO;
import com.openvehicletracking.core.db.DBClientFactory;
import com.openvehicletracking.core.message.MessageHandler;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by oksuz on 07/03/2017.
 */
public class DeviceCommandVerticle extends TrackerAbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceCommandVerticle.class);
    private static final OpenVehicleTracker openVehicleTracker = OpenVehicleTracker.getInstance();
    private static DBClientFactory dbClientFactory = openVehicleTracker.getDbClientFactory();

    private CommandDAO commandDAO;

    @Override
    public void start() throws Exception {
        LOGGER.info("Starting verticle " + DeviceCommandVerticle.class.getName() );
        EventBus eventBus = vertx.eventBus();

        MessageConsumer<String> handler = eventBus.consumer(OpenVehicleTracker.Constant.DEVICE_COMMAND);
        handler.handler(messageHandler());
    }

    private Handler<Message<String>> messageHandler() {
        return message -> {
            MessageHandler handler = findHandler(message.body());
            com.openvehicletracking.core.message.Message deviceMessage = handler.handle(message.body());
            Device device = OpenVehicleTracker.getInstance().getDeviceRegistry().findDevice(deviceMessage.getDevice());

            if (device != null) {
                commandDAO = new CommandDAO(dbClientFactory, deviceMessage.getDeviceId());
                device.replyMessage(deviceMessage, commandDAO, message::reply);
            }
        };
    }
}
