package com.openvehicletracking.collector.http.controller;

import com.google.gson.Gson;
import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.cache.DeviceStateCache;
import com.openvehicletracking.collector.db.FindOrder;
import com.openvehicletracking.collector.db.MongoCollection;
import com.openvehicletracking.collector.db.Query;
import com.openvehicletracking.collector.helper.HttpHelper;
import com.openvehicletracking.collector.http.domain.MessageRequest;
import com.openvehicletracking.collector.http.domain.UserDevice;
import com.openvehicletracking.core.DeviceState;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.web.RoutingContext;

import java.text.ParseException;

/**
 * Created by oksuz on 08/10/2017.
 *
 */
public class MessagesController {

    public void state(RoutingContext context) {
        UserDevice device = context.get("device");
        DeviceState state = DeviceStateCache.getInstance().get(device.getSerial());

        if (state == null) {
            HttpHelper.getNotFound(context.response(), "device state not found");
            return;
        }


        HttpHelper.getOK(context.response(), new Gson().toJson(state)).end();
    }

    public void lastMessages(RoutingContext context) {
        MessageRequest request;
        try {
            request = new MessageRequest(context.request());
        } catch (Exception e) {
            HttpHelper.getBadRequest(context.response(), e.getMessage()).end();
            return;
        }

        FindOptions findOptions = new FindOptions();
        findOptions.setLimit(request.getSize());
        findOptions.setSort(new JsonObject().put("datetime", FindOrder.DESC.getValue()));

        JsonObject queryJson = new JsonObject();
        queryJson.put("deviceId", request.getDeviceId());

        if (request.getGpsStatus() != null) {
            queryJson.put("status", request.getGpsStatus());
        }

        try {
            if (null != request.getFromDate() && null != request.getToDate()) {
                JsonObject datetimeCond = new JsonObject()
                        .put("$gte", request.getFromDate().getTime())
                        .put("$lte", request.getToDate().getTime());
                queryJson.put("datetime", datetimeCond);
            }
        } catch (ParseException e) {
            HttpHelper.getBadRequest(context.response(), "invalid date format. date format must be " + MessageRequest.DATE_FORMAT).end();
            return;
        }

        Query query = new Query(MongoCollection.MESSAGES, queryJson, findOptions);

        context.vertx().eventBus().<JsonArray>send(AppConstants.Events.NEW_QUERY, query, result -> {
            JsonArray messagesResult = result.result().body();
            HttpHelper.getOK(context.response(), messagesResult.toString()).end();
        });

    }
}
