package com.openmts.collector.controller;

import com.openmts.collector.helper.HttpHelper;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by yo on 09/07/2017.
 */
public class UserCheckPointController extends AbstractController {

    public UserCheckPointController(RoutingContext context) {
        super(context);
    }

    @Override
    public void response() {
        HttpHelper.getOKNoContent(routingContext.response()).end();
    }
}
