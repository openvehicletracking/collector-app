package com.openvehicletracking.collector.processor.impl;

import com.openvehicletracking.collector.processor.MessageProcessor;
import com.openvehicletracking.core.json.JsonSerializeable;
import com.openvehicletracking.core.protocol.Command;
import com.openvehicletracking.core.protocol.LocationMessage;
import io.vertx.core.eventbus.EventBus;

public class MessageProcessorImpl implements MessageProcessor {

    private final EventBus eventBus;

    public MessageProcessorImpl(EventBus eventBus) {
        this.eventBus = eventBus;
    }


    @Override
    public void process(LocationMessage message) {
        if (message != null) {
            System.out.println(message.asJson());
        }
    }

    @Override
    public void process(Command message) {
        if (message != null) {
            System.out.println(message.asJson());
        }
    }

    @Override
    public void process(Object message) {
        if (message instanceof LocationMessage) {
            process((LocationMessage) message);
        } else if (message instanceof Command) {
            process((Command) message);
        }
    }

}
