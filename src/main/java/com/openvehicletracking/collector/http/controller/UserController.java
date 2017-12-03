package com.openvehicletracking.collector.http.controller;

import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.db.MongoCollection;
import com.openvehicletracking.collector.db.Query;
import com.openvehicletracking.collector.db.Record;
import com.openvehicletracking.collector.db.UpdateResult;
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
public class UserController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    public UserController(JsonObject config) {
        super(config);
    }
    public void user(RoutingContext context) {
        User user = context.get("user");
        user.setPassword("");
        HttpHelper.getOK(context.response(), user.asJsonString()).end();
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


        Query query = new Query(MongoCollection.USERS)
                .addCondition("email", username)
                .addCondition("password", password)
                .setFindOne(true);

        context.vertx().eventBus().<JsonObject>send(AppConstants.Events.NEW_QUERY, query, result -> {
            if (result.failed()) {
                HttpHelper.getInternalServerError(context.response(), result.cause().getMessage()).end();
                return;
            }

            JsonObject userResult = result.result().body();
            if (userResult == null) {
                HttpHelper.getNotFound(context.response(), "user not found").end();
                return;
            }

            User user = User.fromMongoRecord(userResult);

            AccessToken token = AccessToken.createFor24Hours(user.getEmail());
            Query updateCondition = new Query(MongoCollection.ACCESS_TOKENS).addCondition("email", user.getEmail());
            Record record = new Record(MongoCollection.ACCESS_TOKENS, token.toMongoRecord())
                    .setCondition(updateCondition)
                    .isUpsert(true);
            context.vertx().eventBus().<UpdateResult>send(AppConstants.Events.UPDATE, record, res -> {
                if (res.failed()) {
                    HttpHelper.getInternalServerError(context.response(), res.cause().getMessage()).end();
                    return;
                }

                HttpHelper.getOK(context.response(), token.asJsonString()).end();
            });
        });
    }

    public void logout(RoutingContext context) {
        User user = context.get("user");
        Query deleteAccessTokenQuery = new Query(MongoCollection.ACCESS_TOKENS).addCondition("email", user.getEmail());
        context.vertx().eventBus().<JsonObject>send(AppConstants.Events.DELETE, deleteAccessTokenQuery);
        HttpHelper.getOKNoContent(context.response()).end();
    }

}
