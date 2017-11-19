package com.openvehicletracking.collector.http.controller;

import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.cache.DeviceStateCache;
import com.openvehicletracking.collector.db.MongoCollection;
import com.openvehicletracking.collector.db.Query;
import com.openvehicletracking.collector.db.Record;
import com.openvehicletracking.collector.helper.HttpHelper;
import com.openvehicletracking.collector.http.domain.HashCreateRequest;
import com.openvehicletracking.collector.http.domain.User;
import com.openvehicletracking.collector.http.domain.UserDevice;
import com.openvehicletracking.core.DeviceState;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Date;
import java.util.Objects;

/**
 * Created by oksuz on 18/11/2017.
 *
 */
public class PublicLocationController {

    public void publicLocation(RoutingContext context) {
        HttpServerRequest request = context.request();
        HttpServerResponse response = context.response();

        String hash = request.getParam("hash");
        Query query = new Query(MongoCollection.PUBLIC_LOCATION_HASH)
                .addCondition("hash", hash)
                .addCondition("isActive", true)
                .addCondition("expireDate", new JsonObject().put("$gte", new Date().getTime()))
                .setFindOne(true);

        context.vertx().eventBus().<JsonObject>send(AppConstants.Events.NEW_QUERY, query, hashResult -> {
            if (hashResult.failed()) {
                HttpHelper.getInternalServerError(response, "hash query failed").end();
                return;
            }

            JsonObject publicDevice = hashResult.result().body();
            if (publicDevice == null) {
                HttpHelper.getNotFound(response, "invalid hash").end();
                return;
            }

            DeviceState state = DeviceStateCache.getInstance().get(publicDevice.getString("deviceId"));
            if (state == null) {
                // @TODO Create device state from last valid message
                HttpHelper.getInternalServerError(response, "device state not found! why?").end();
                return;
            }

            Query deviceQuery = new Query(MongoCollection.USERS)
                    .addCondition("devices.serial", state.getDeviceId());

            context.vertx().eventBus().<JsonArray>send(AppConstants.Events.NEW_QUERY, deviceQuery, deviceQueryResult -> {
                if (deviceQueryResult.failed()) {
                    HttpHelper.getInternalServerError(response, "device query failed").end();
                    return;
                }

                JsonArray userList = deviceQueryResult.result().body();
                User user = User.fromMongoRecord(userList.getJsonObject(0));
                UserDevice userDevice = user.getDevices().stream().filter(device -> Objects.equals(state.getDeviceId(), device.getSerial())).findAny().get();

                JsonObject hashResponse = new JsonObject();
                hashResponse.put("latitude", state.getLatitude());
                hashResponse.put("longitude", state.getLongitude());
                hashResponse.put("direction", state.getDirection());
                hashResponse.put("speed", state.getSpeed());
                hashResponse.put("label", userDevice.getLabel());

                HttpHelper.getOK(response, hashResponse).end();
            });
        });
    }

    public void createHash(RoutingContext context) {
        HttpServerResponse response = context.response();
        HashCreateRequest hashCreateRequest = new HashCreateRequest(context);

        JsonObject hashRecord = new JsonObject();
        hashRecord.put("deviceId", hashCreateRequest.getDeviceId())
                .put("expireDate", hashCreateRequest.getExpireDate().getTime())
                .put("hash", hashCreateRequest.createHashForRequest())
                .put("isActive", true);

        Record record = new Record(MongoCollection.PUBLIC_LOCATION_HASH, hashRecord);
        context.vertx().eventBus().send(AppConstants.Events.PERSIST, record);
        HttpHelper.getOK(response, hashRecord).end();
    }
}
