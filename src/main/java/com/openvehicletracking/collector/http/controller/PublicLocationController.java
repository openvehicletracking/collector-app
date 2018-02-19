package com.openvehicletracking.collector.http.controller;

import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.cache.DeviceStateCache;
import com.openvehicletracking.collector.db.MongoCollection;
import com.openvehicletracking.collector.db.Query;
import com.openvehicletracking.collector.db.Record;
import com.openvehicletracking.collector.helper.HttpHelper;
import com.openvehicletracking.collector.http.domain.HashCreateRequest;
import com.openvehicletracking.collector.http.domain.ShareLocationWithSmsRequest;
import com.openvehicletracking.collector.http.domain.User;
import com.openvehicletracking.collector.http.domain.UserDevice;
import com.openvehicletracking.collector.notification.SendResult;
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
public class PublicLocationController extends AbstractController {

    public PublicLocationController(JsonObject config) {
        super(config);
    }

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
                HttpHelper.getInternalServerError(response, "device state not found!").end();
                return;
            }

            JsonObject hashResponse = new JsonObject();
            hashResponse.put("latitude", state.getLatitude());
            hashResponse.put("longitude", state.getLongitude());
            hashResponse.put("direction", state.getDirection());
            hashResponse.put("speed", state.getSpeed());
            hashResponse.put("label", publicDevice.getString("label"));

            HttpHelper.getOK(response, hashResponse).end();
        });
    }

    public void createHash(RoutingContext context) {
        HttpServerResponse response = context.response();
        HashCreateRequest hashCreateRequest = new HashCreateRequest(context);

        JsonObject hashRecord = new JsonObject();
        hashRecord.put("deviceId", hashCreateRequest.getDeviceId())
                .put("expireDate", hashCreateRequest.getExpireDate().getTime())
                .put("hash", hashCreateRequest.createHashForRequest())
                .put("label", hashCreateRequest.getDeviceLabel())
                .put("isActive", true);

        Record record = new Record(MongoCollection.PUBLIC_LOCATION_HASH, hashRecord);
        context.vertx().eventBus().<String>send(AppConstants.Events.PERSIST, record, result -> {
            if (result.succeeded() && result.result() != null) {
                String docId = result.result().body();
                hashRecord.put("docId", docId);
                HttpHelper.getOK(response, hashRecord).end();
            } else {
                String cause = (result.cause() != null) ? result.cause().getMessage() : "cannot create document";
                HttpHelper.getInternalServerError(response, cause).end();
            }
        });
    }

    public void shareLocationWithSms(RoutingContext context) {
        final HttpServerResponse response = context.response();
        final ShareLocationWithSmsRequest smsRequest = new ShareLocationWithSmsRequest(context);

        JsonObject smsConfig = getConfig().getJsonObject("sms");
        String number, hash, smsBody, publicLocationUrl, label;
        try {
            number = smsRequest.getSmsNumber();
            hash = smsRequest.getHash();
        } catch (Exception e) {
            HttpHelper.getBadRequest(response, e.getMessage()).end();
            return;
        }

        publicLocationUrl = String.format(getConfig().getString("publicLocationUrl"), hash);
        label = ((UserDevice) context.get("device")).getLabel();
        smsBody = smsConfig.getString("locationShareMessage")
                .replace("%URL%", publicLocationUrl)
                .replace("%LABEL%", label);

        JsonObject sendSmsRequest = new JsonObject().put("gsm", number).put("body", smsBody);

        context.vertx().eventBus().<SendResult>send(AppConstants.Events.NEW_SMS, sendSmsRequest, sendResult -> {
            if (sendResult.failed()) {
                String err = sendResult.cause() != null ? sendResult.cause().getMessage() : "Internal Server Error";
                HttpHelper.getInternalServerError(response, err).end();
                return;
            }

            SendResult result = sendResult.result().body();
            if (result.succeed()) {
                HttpHelper.getOK(response, new JsonObject().put("response", result.getResult())).end();
                return;
            }

            if (result.failed()) {
                HttpHelper.getBadRequest(response, result.getCause().getMessage()).end();
            }
        });
    }
}
