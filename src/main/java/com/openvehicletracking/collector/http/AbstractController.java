package com.openvehicletracking.collector.http;

import io.vertx.ext.web.RoutingContext;

/**
 * Created by oksuz on 07/10/2017.
 *
 */
public abstract class AbstractController {

    private RoutingContext context;

    public AbstractController(RoutingContext context) {
        this.context = context;
    }

    public abstract void handle();

}
