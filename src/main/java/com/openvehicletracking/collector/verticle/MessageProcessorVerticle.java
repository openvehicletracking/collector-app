package com.openvehicletracking.collector.verticle;

import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.Config;
import com.openvehicletracking.collector.database.MongoCollection;
import com.openvehicletracking.collector.database.Record;
import com.openvehicletracking.core.CacheKeyGenerator;
import com.openvehicletracking.core.DeviceState;
import com.openvehicletracking.core.exception.StateCreateNotSupportException;
import com.openvehicletracking.core.protocol.Message;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class MessageProcessorVerticle extends AbstractMessageProcessorVerticle {

    private static Logger LOGGER = LoggerFactory.getLogger(TcpVerticle.class);
    private RedisClient redisClient;

    public MessageProcessorVerticle(NetSocket socket) {
        super(socket);
    }

    @Override
    public void handler(Buffer buffer) {
        Message deviceMessage = getMessage(buffer);
        if (deviceMessage == null) {
            return;
        }

        JsonObject message = createJsonMessage(deviceMessage);
        if (message != null) {
            vertx.eventBus().<JsonObject>send(AppConstants.Events.PERSIST, new Record(MongoCollection.MESSAGES, message), result -> {
                LOGGER.info(result.result().body().encodePrettily());
            });
        }

        DeviceState state = null;
        try {
            state = deviceMessage.createState();
        } catch (StateCreateNotSupportException ignored) {}

        if (state == null || device == null) {
            LOGGER.info("State or device is null, device: {}, state: {}", device, state);
            return;
        }

        final String stateAsJson = state.asJson();
        redisClient.set(CacheKeyGenerator.stateCacheKey(device.getId()), stateAsJson, handler -> {
            if (handler.succeeded()) {
                LOGGER.debug("state write for deviceId: {}, state: {}, result: {}", device.getId(), stateAsJson, handler.result());
            }
        });

    }

    @Override
    public void start() throws Exception {
        super.start();
        RedisOptions redisOptions = new RedisOptions();
        redisOptions.setHost(Config.getInstance().getString("cache.redis.host", "localhost"))
                .setPort(Config.getInstance().getInt("cache.redis.port", 6379))
                .setEncoding("UTF-8");

        redisClient = RedisClient.create(vertx, redisOptions);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        redisClient.close(handler -> LOGGER.debug("redis connection closed for device {}", device.getId()));
    }
}
