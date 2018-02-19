package com.openvehicletracking.collector.notification;


import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

public abstract class AbstractSender implements Sender {

    private JsonObject config;
    private WebClient client;

    public AbstractSender(JsonObject config, WebClient client) {
        this.config = config;
        this.client = client;
    }

    public JsonObject getConfig() {
        return new JsonObject(config.toString());
    }

    public WebClient getClient() {
        return client;
    }
}
