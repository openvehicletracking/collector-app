package com.openvehicletracking.collector.http.controller;

import com.openvehicletracking.collector.helper.HttpHelper;
import com.openvehicletracking.collector.http.domain.UserDevice;
import com.openvehicletracking.core.GsonFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class DeviceController extends AbstractController {

    public DeviceController(JsonObject config) {
        super(config);
    }

    public void info(RoutingContext context) {
        UserDevice d = context.get("device");
        HttpHelper.getOK(context.response(), GsonFactory.getGson().toJson(d)).end();
    }
}
