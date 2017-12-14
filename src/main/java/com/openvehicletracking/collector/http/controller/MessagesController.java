package com.openvehicletracking.collector.http.controller;

import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.cache.DeviceStateCache;
import com.openvehicletracking.collector.db.FindOrder;
import com.openvehicletracking.collector.db.MongoCollection;
import com.openvehicletracking.collector.db.Query;
import com.openvehicletracking.collector.helper.HttpHelper;
import com.openvehicletracking.collector.http.domain.MessageRequest;
import com.openvehicletracking.collector.http.domain.MessagesResponse;
import com.openvehicletracking.collector.http.domain.UserDevice;
import com.openvehicletracking.core.Device;
import com.openvehicletracking.core.DeviceRegistry;
import com.openvehicletracking.core.DeviceState;
import com.openvehicletracking.core.GpsStatus;
import com.openvehicletracking.core.exception.UnsupportedMessageTypeException;
import com.openvehicletracking.core.geojson.GeoJsonResponse;
import com.openvehicletracking.core.message.LocationMessage;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by oksuz on 08/10/2017.
 *
 */
public class MessagesController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagesController.class);

    public MessagesController(JsonObject config) {
        super(config);
    }

    public void state(RoutingContext context) {
        UserDevice device = context.get("device");
        DeviceState state = DeviceStateCache.getInstance().get(device.getSerial());

        if (state != null) {
            HttpHelper.getOK(context.response(), state.asJsonString()).end();
            return;
        }

        LOGGER.debug("in-memory device state not found trying to generate from last valid db message");
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
                LOGGER.debug("Last valid db message found for creating state {}", message.encodePrettily());
                try {
                    DeviceState stateFromDbMessage = deviceImpl.createStateFromMessage(locationMessage);
                    LOGGER.debug("Created device state {}", stateFromDbMessage);
                    DeviceStateCache.getInstance().put(stateFromDbMessage);
                    HttpHelper.getOK(context.response(), stateFromDbMessage.asJsonString()).end();
                } catch (Exception ignored) {
                    HttpHelper.getInternalServerError(context.response(), "device state not found");
                }
            } else {
                HttpHelper.getNotFound(context.response(), "device state not found").end();
            }
        });
    }

    public void lastMessages(RoutingContext context) {
        Query query = createQueryFromMessageRequest(context);
        if (query == null) {
            return;
        }

        final MessagesResponse messagesResponse = new MessagesResponse(context.get("device"));
        context.vertx().eventBus().<JsonArray>send(AppConstants.Events.NEW_QUERY, query, result -> {
            messagesResponse.setMessages(result.result().body());
            HttpHelper.getOK(context.response(), messagesResponse.getResponse()).end();
        });
    }

    public void asGeoJson(RoutingContext context) {
        Query query = createQueryFromMessageRequest(context);
        if (query == null) {
            return;
        }

        UserDevice userDevice = context.get("device");
        Device device = DeviceRegistry.getInstance().findDevice(userDevice.getDevice());
        context.vertx().eventBus().<JsonArray>send(AppConstants.Events.NEW_QUERY, query, result -> {
            JsonArray jsonMessages = result.result().body();
            ArrayList<LocationMessage> messages = new ArrayList<>();
            jsonMessages.forEach(o -> messages.add(LocationMessage.fromJson(o.toString(), device.getLocationType())));
            GeoJsonResponse response = device.responseAsGeoJson(messages);
            HttpHelper.getOK(context.response(), response.asJsonString()).end();
        });
    }

    private Query createQueryFromMessageRequest(RoutingContext context) {
        MessageRequest request;
        try {
            request = new MessageRequest(context.request());
        } catch (Exception e) {
            HttpHelper.getBadRequest(context.response(), e.getMessage()).end();
            return null;
        }

        Query query = new Query(MongoCollection.MESSAGES);
        query.addCondition("deviceId", request.getDeviceId());

        if (request.getGpsStatus() != null) {
            query.addCondition("status", request.getGpsStatus());
        }

        try {
            JsonObject datetimeCond = new JsonObject();
            if (null != request.getFromDate()) {
                datetimeCond.put("$gte", request.getFromDate().getTime());
            }

            if (null != request.getFromDate() && null != request.getToDate()) {
                datetimeCond.put("$lte", request.getToDate().getTime());
            }

            if (!datetimeCond.isEmpty()) {
                query.addCondition("datetime", datetimeCond);
            }
        } catch (ParseException e) {
            HttpHelper.getBadRequest(context.response(), "invalid date format. date format must be " + MessageRequest.DATE_FORMAT).end();
            return null;
        }

        query.setLimit(request.getSize()).addSort("datetime", FindOrder.DESC);
        return query;
    }
}
