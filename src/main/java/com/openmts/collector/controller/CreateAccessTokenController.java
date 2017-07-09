package com.openmts.collector.controller;

import com.openmts.collector.domain.LoginRequest;
import com.openmts.collector.helper.HttpHelper;
import com.openmts.collector.helper.MongoHelper;
import com.openmts.core.Motodev;
import com.openmts.core.db.Collection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.UUID;

/**
 * Created by oksuz on 08/07/2017.
 *
 */
public class CreateAccessTokenController extends AbstractController {

    private MongoClient client;

    public CreateAccessTokenController(RoutingContext context) {
        super(context);
    }

    @Override
    public void response() {
        client = Motodev.getInstance().newDbClient();
        LoginRequest loginRequest = new LoginRequest(routingContext);
        MongoHelper.Query query = MongoHelper.getUserQuery(loginRequest.getUsername(), loginRequest.getEncodedPassword());
        client.find(Collection.USERS, query.getQuery(), getFindUserHandler(query));
    }

    private Handler<AsyncResult<List<JsonObject>>> getFindUserHandler(MongoHelper.Query query) {
        return result -> {
            if (result.succeeded() && result.result() != null && result.result().size() > 0) {
                JsonObject user = result.result().get(0);
                String accessToken = UUID.randomUUID().toString();
                JsonObject update = new JsonObject().put("$set", new JsonObject().put("accessToken", accessToken));
                client.updateCollection(Collection.USERS, query.getQuery(), update, getUserUpdateHandler(user, accessToken));
            } else if (result.failed()) {
                HttpHelper.getInternalServerError(routingContext.response(), result.cause().getMessage()).end();
            } else {
                HttpHelper.getNotFound(routingContext.response(), "user not found").end();
            }
            client.close();
        };
    }

    private Handler<AsyncResult<MongoClientUpdateResult>> getUserUpdateHandler(JsonObject user, String accessToken) {
        return updateResult -> {
            if (updateResult.succeeded()) {
                user.put("accessToken", accessToken);
                user.remove("password");
                HttpHelper.getOK(routingContext.response(), user.toString()).end();
            } else if (updateResult.failed()) {
                HttpHelper.getInternalServerError(routingContext.response(), updateResult.cause().getMessage()).end();
            }

            client.close();
        };
    }
}
