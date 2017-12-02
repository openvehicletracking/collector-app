package com.openvehicletracking.collector.http.controller;

import io.vertx.core.json.JsonObject;

public abstract class AbstractController {

    JsonObject config;

    public AbstractController(JsonObject config) {
        this.config = config;
    }

    public JsonObject getConfig() {
        return new JsonObject(config.toString());
    }
}
