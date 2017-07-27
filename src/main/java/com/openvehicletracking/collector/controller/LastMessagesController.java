package com.openvehicletracking.collector.controller;

import com.openvehicletracking.collector.helper.HttpHelper;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by oksuz on 09/07/2017.
 */
public class LastMessagesController extends AbstractLastMessagesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LastMessagesController.class);

    public LastMessagesController(RoutingContext context) {
        super(context);
    }

    @Override
    public void response() {
        lastMessages(routingContext, result -> {
            if (result.failed()) {
                LOGGER.error("an error occurred while making query", result.cause());
                HttpHelper.getInternalServerError(routingContext.response(), "internal server error").end();
                return;
            }

            HttpHelper.getOK(routingContext.response(), result.result().toString()).end();
        });
    }
}
