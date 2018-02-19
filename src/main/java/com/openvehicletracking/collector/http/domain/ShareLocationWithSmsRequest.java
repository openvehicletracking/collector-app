package com.openvehicletracking.collector.http.domain;

import io.vertx.ext.web.RoutingContext;

public class ShareLocationWithSmsRequest {

    private final RoutingContext context;

    public ShareLocationWithSmsRequest(final RoutingContext context) {
        this.context = context;
    }

    public String getSmsNumber() {
        return context.getBodyAsJson().getString("number");
    }

    public String getHash() {
        return context.getBodyAsJson().getString("hash");
    }
}
