package com.openvehicletracking.collector.verticle;

import com.openvehicletracking.collector.Context;
import com.openvehicletracking.collector.NetSocketConnectionHolder;
import com.openvehicletracking.core.ConnectionHolder;
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

public abstract  class AbstractMessageProcessorVerticle extends AbstractVerticle {

    protected NetSocket socket;
    protected MessagingProtocol protocol;
    protected Device device;

    public AbstractMessageProcessorVerticle(NetSocket socket) {
        this.socket = socket;
    }

    protected Message getMessage(Buffer buffer) {
        ConnectionHolder connectionHolder = new NetSocketConnectionHolder(socket);
        Message message = null;
        if (protocol == null) {
            message = Context.getProtocolChain().handle(buffer.getBytes(), connectionHolder);
            if (message != null) {
                protocol = Context.getProtocolChain().find(message.getProtocolName());
            }
        } else {
            byte[] messageAsByte = buffer.getBytes();
            Optional<MessageHandler> handler = protocol.getHandlers().stream().filter(messageHandler -> messageHandler.isMatch(messageAsByte)).findFirst();
            if (handler.isPresent()) {
                message = handler.get().handle(messageAsByte, connectionHolder);
            }
        }

        if (message != null && message.getDevice() != null && message.getDevice().getId() != null) {
            device = message.getDevice();
        }

        return message;
    }


    @Override
    public void start() throws Exception {
        socket.handler(this::handler);
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

            return message;
        }
        return null;
    }

    public abstract void handler(Buffer buffer);


}
