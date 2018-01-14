package com.openvehicletracking.collector.processor;

import com.openvehicletracking.core.protocol.Command;
import com.openvehicletracking.core.protocol.LocationMessage;

public interface MessageProcessor extends Processor {

    void process(LocationMessage message);

    void process(Command message);

}
