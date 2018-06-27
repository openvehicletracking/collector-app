package com.openvehicletracking.collector.verticle;

import com.openvehicletracking.collector.Context;
import com.openvehicletracking.collector.NetSocketConnectionHolder;
import com.openvehicletracking.core.Device;
import com.openvehicletracking.core.protocol.Message;
import com.openvehicletracking.core.protocol.MessageHandler;
import com.openvehicletracking.core.protocol.MessagingProtocol;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class AbstractMessageProcessorVerticle extends AbstractVerticle {

    protected NetSocketConnectionHolder connectionHolder;
    protected MessagingProtocol protocol;
    protected Device device;

    public AbstractMessageProcessorVerticle(NetSocket socket) {
        this.connectionHolder = new NetSocketConnectionHolder(socket);
    }

    protected Message getMessage(Buffer buffer) {
        byte[] messageAsByte = buffer.getBytes();
        if (device == null || protocol == null) {
            detectProtocolAndDeviceFromMessage(messageAsByte);
        }

        if (protocol != null && device != null) {
            Optional<MessageHandler> handler = protocol.getHandlers()
                    .stream()
                    .filter(messageHandler -> messageHandler.isMatch(messageAsByte))
                    .findFirst();


            if (handler.isPresent()) {
                 return handler.get().handle(messageAsByte, connectionHolder);
            }
        }

        return null;
    }

    protected void detectProtocolAndDeviceFromMessage(byte[] m) {
        Message message = Context.getProtocolChain().handle(m, connectionHolder);
        if (message != null && message.getDevice() != null && message.getDevice().getId() != null) {
            protocol = Context.getProtocolChain().find(message.getProtocolName());
            device = message.getDevice();
        }
    }


    @Override
    public void start() throws Exception {
        connectionHolder.getConnection().handler(this::handler);
    }

    @Nullable
    protected JsonObject createJsonMessage(Message deviceMessage) {
        String jsonString = deviceMessage.asJson();

        if (jsonString != null && jsonString.trim().charAt(0) == '{') {
            JsonObject message = new JsonObject(jsonString.trim());

            message.remove("device");
            JsonObject jsonDevice = new JsonObject().put("deviceId", device.getId()).put("name", device.getName());
            message.put("device", jsonDevice);

            if (!message.containsKey("protocolName")) {
                message.put("protocolName", deviceMessage.getProtocolName());
            }

            if (!message.containsKey("messageType")) {
                message.put("messageType", deviceMessage.getType());
            }

            if (!message.containsKey("datetime") && deviceMessage.getDate() != null){
                message.put("datetime", deviceMessage.getDate().getTime());
            }

            return message;
        }
        return null;
    }

    public abstract void handler(Buffer buffer);


}
