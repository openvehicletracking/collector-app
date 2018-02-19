package com.openvehicletracking.collector.http.domain;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MessagesResponse {
    
    private JsonObject response = new JsonObject();
    
    public MessagesResponse(UserDevice device, JsonArray messages) {
        response.put("device", device.getDevice());
        response.put("label", device.getLabel());
        response.put("serial", device.getSerial());
        response.put("messages", messages);
    }

    public MessagesResponse(UserDevice device) {
        response.put("device", device.getDevice());
        response.put("label", device.getLabel());
        response.put("serial", device.getSerial());
    }

    public void setMessages(JsonArray messages) {
        response.put("messages", messages);
    }

    public JsonObject getResponse() {
        return response;
    }
}
