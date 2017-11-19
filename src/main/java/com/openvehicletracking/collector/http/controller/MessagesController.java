package com.openvehicletracking.collector.http.controller;

import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.cache.DeviceStateCache;
import com.openvehicletracking.collector.db.FindOrder;
import com.openvehicletracking.collector.db.MongoCollection;
import com.openvehicletracking.collector.db.Query;
import com.openvehicletracking.collector.helper.HttpHelper;
import com.openvehicletracking.collector.http.domain.MessageRequest;
import com.openvehicletracking.collector.http.domain.UserDevice;
import com.openvehicletracking.core.Device;
import com.openvehicletracking.core.DeviceRegistry;
import com.openvehicletracking.core.DeviceState;
import com.openvehicletracking.core.GpsStatus;
import com.openvehicletracking.core.exception.UnsupportedMessageTypeException;
import com.openvehicletracking.core.message.LocationMessage;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

/**
 * Created by oksuz on 08/10/2017.
 *
 */
public class MessagesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagesController.class);

    public void state(RoutingContext context) {
        UserDevice device = context.get("device");
        DeviceState state = DeviceStateCache.getInstance().get(device.getSerial());

        if (state != null) {
            HttpHelper.getOK(context.response(), state.asJsonString()).end();
            return;
        }

        Device deviceImpl = DeviceRegistry.getInstance().findDevice(device.getDevice());

        Query deviceMessageQuery = new Query(MongoCollection.MESSAGES)
                .addCondition("deviceId", device.getSerial())
                .addCondition("device", device.getDevice())
                .addCondition("status", GpsStatus.VALID)
                .addSort("datetime", FindOrder.DESC)
                .setLimit(1);

        context.vertx().eventBus().<JsonArray>send(AppConstants.Events.NEW_QUERY, deviceMessageQuery, messageResult -> {
            if (messageResult.result().body() != null && messageResult.result().body().size() > 0) {
                JsonObject message = messageResult.result().body().getJsonObject(0);
                LocationMessage locationMessage = LocationMessage.fromJson(message.toString(), deviceImpl.getLocationType());
                try {
                    DeviceState stateFromDbMessage = deviceImpl.createStateFromMessage(locationMessage);
                    DeviceStateCache.getInstance().put(stateFromDbMessage);
                    HttpHelper.getOK(context.response(), stateFromDbMessage.asJsonString()).end();
                } catch (UnsupportedMessageTypeException ignored) {
                    HttpHelper.getInternalServerError(context.response(), "device state not found");
                }
            } else {
                HttpHelper.getNotFound(context.response(), "device state not found").end();
            }
        });
    }

    public void lastMessages(RoutingContext context) {
        MessageRequest request;
        try {
            request = new MessageRequest(context.request());
        } catch (Exception e) {
            HttpHelper.getBadRequest(context.response(), e.getMessage()).end();
            return;
        }

        Query query = new Query(MongoCollection.MESSAGES);
        query.addCondition("deviceId", request.getDeviceId());

        if (request.getGpsStatus() != null) {
            query.addCondition("status", request.getGpsStatus());
        }

        try {
            if (null != request.getFromDate() && null != request.getToDate()) {
                JsonObject datetimeCond = new JsonObject()
                        .put("$gte", request.getFromDate().getTime())
                        .put("$lte", request.getToDate().getTime());

                query.addCondition("datetime", datetimeCond);
            }
        } catch (ParseException e) {
            HttpHelper.getBadRequest(context.response(), "invalid date format. date format must be " + MessageRequest.DATE_FORMAT).end();
            return;
        }

        query.setLimit(request.getSize()).addSort("datetime", FindOrder.DESC);

        context.vertx().eventBus().<JsonArray>send(AppConstants.Events.NEW_QUERY, query, result -> {
            JsonArray messagesResult = result.result().body();
            HttpHelper.getOK(context.response(), messagesResult.toString()).end();
        });
    }

}
