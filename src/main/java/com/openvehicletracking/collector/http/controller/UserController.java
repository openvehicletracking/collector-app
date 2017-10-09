package com.openvehicletracking.collector.http.controller;

import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.db.*;
import com.openvehicletracking.collector.helper.HttpHelper;
import com.openvehicletracking.collector.http.domain.AccessToken;
import com.openvehicletracking.collector.http.domain.LoginRequest;
import com.openvehicletracking.collector.http.domain.User;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by oksuz on 08/10/2017.
 *
 */
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    public void user(RoutingContext context) {
        User user = context.get("user");
        user.setPassword("");
        HttpHelper.getOK(context.response(), user.toJson()).end();
    }

    public void checkpoint(RoutingContext context) {
        HttpHelper.getOKNoContent(context.response()).end();
    }

    public void login(RoutingContext context) {
        LoginRequest request = new LoginRequest(context);

        String username = "", password = "";
        try {
            username = request.getUsername();
            password = request.getEncodedPassword();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ignored) {}


        JsonObject queryJson = new JsonObject().put("email", username).put("password", password);
        Query query = new Query(MongoCollection.USERS, queryJson).setFindOne(true);

        context.vertx().eventBus().<JsonObject>send(AppConstants.Events.NEW_QUERY, query, result -> {
            JsonObject userResult = result.result().body();
            if (result.failed()) {
                HttpHelper.getInternalServerError(context.response(), result.cause().getMessage()).end();
                return;
            }

            if (userResult == null) {
                HttpHelper.getNotFound(context.response(), "user not found").end();
                return;
            }

            User user = User.fromJson(userResult);
            AccessToken token = AccessToken.createFor2Hours();

            Record record = new Record(MongoCollection.USERS, new JsonObject().put("$push", new JsonObject().put("accessTokens", JsonObject.mapFrom(token))))
                .setUpdateQuery(new Query(MongoCollection.USERS, new JsonObject().put("id", user.getId())));

            context.vertx().eventBus().<UpdateResult>send(AppConstants.Events.UPDATE, record, res -> {
                UpdateResult updateResult = res.result().body();

                if (res.failed()) {
                    HttpHelper.getInternalServerError(context.response(), res.cause().getMessage()).end();
                } else if (updateResult.getDocModified() > 0) {
                    HttpHelper.getOK(context.response(), token.toJson()).end();
                } else {
                    HttpHelper.getInternalServerError(context.response(), "unkown error").end();
                }
            });
        });

    }

}
