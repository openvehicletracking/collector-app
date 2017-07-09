package com.openmts.collector.controller;

import com.openmts.collector.helper.HttpHelper;
import com.openmts.core.Motodev;
import com.openmts.core.db.Collection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

/**
 * Created by yo on 09/07/2017.
 */
public class DeviceStateController extends AbstractController {

    private MongoClient client;

    public DeviceStateController(RoutingContext context) {
        super(context);
    }

    @Override
    public void response() {
        String deviceId = routingContext.request().getParam("deviceId");
        JsonObject user = routingContext.get("user");
        if (!user.getJsonArray("devices").contains(deviceId)) {
            HttpHelper.getUnauthorized(routingContext.response());
            return;
        }

        client = Motodev.getInstance().newDbClient();
        JsonObject query = new JsonObject().put("deviceId", deviceId);
        client.find(Collection.DEVICE_META, query, getDeviceMetaHandler());
    }

    private Handler<AsyncResult<List<JsonObject>>> getDeviceMetaHandler() {
        return result -> {
            if (result.failed()) {
                HttpHelper.getInternalServerError(routingContext.response(), result.cause().getMessage()).end();
                client.close();
                return;
            }

            if (result.result() != null && result.result().size() > 0) {
                JsonObject meta = result.result().get(0);
                HttpHelper.getOK(routingContext.response(), meta.toString()).end();
                client.close();
                return;
            }

            HttpHelper.getNotFound(routingContext.response(), "not found").end();
            client.close();
        };
    }
}
