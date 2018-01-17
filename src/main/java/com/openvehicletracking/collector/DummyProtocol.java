package com.openvehicletracking.collector;

import com.openvehicletracking.core.ConnectionHolder;
import com.openvehicletracking.core.protocol.Message;
import com.openvehicletracking.core.protocol.MessageHandler;
import com.openvehicletracking.core.protocol.MessagingProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class DummyProtocol implements MessagingProtocol {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyProtocol.class);
    private final ArrayList<MessageHandler> handlers = new ArrayList<>();

    public DummyProtocol() {
        handlers.add(new DummyHandler());
    }

    @Override
    public String getName() {
        return "dummy";
    }

    @Override
    public ArrayList<MessageHandler> getHandlers() {
        return handlers;
    }

    public static class DummyHandler implements MessageHandler {
        @Override
        public boolean isMatch(Object msg) {
            log(msg);
            return false;
        }

        private void log(Object msg) {
            if (msg instanceof String) {
                LOGGER.info("new message " + msg);
            } else if (msg instanceof byte[]) {
                byte[] message = (byte[]) msg;

                ByteBuffer buffer = ByteBuffer.wrap(message);
                StringBuilder hex = new StringBuilder();
                StringBuilder byteArr = new StringBuilder();
                byteArr.append("[");
                while (buffer.hasRemaining()) {
                    byte curr = buffer.get();
                    hex.append("0x").append(String.format("%x", curr)).append(",");
                    byteArr.append(curr).append(",");
                }

                byteArr.append("]");
                LOGGER.info("message as hex {}", hex.toString());
                LOGGER.info("message as byteArr {}", byteArr.toString());
                LOGGER.info("message as string {}", new String(message));
            }
        }

        @Override
        public Message handle(Object msg, ConnectionHolder<?> connectionHolder) {
            return null;
        }
    }
}
