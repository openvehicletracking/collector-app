package com.openvehicletracking.collector.controller;

import com.openvehicletracking.collector.helper.HttpHelper;
import com.openvehicletracking.core.adapter.GeoJsonResponseAdapter;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by oksuz on 09/07/2017.
 */
public class LastMessagesGeoJsonController extends AbstractLastMessagesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LastMessagesGeoJsonController.class);

    public LastMessagesGeoJsonController(RoutingContext context) {
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

            GeoJsonResponseAdapter geoJsonAdapter = new GeoJsonResponseAdapter();
            HttpHelper.getOK(routingContext.response(), geoJsonAdapter.result(result.result()).toString()).end();
        });
    }


}
