package com.openvehicletracking.collector.http.filter;

import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.db.MongoCollection;
import com.openvehicletracking.collector.db.Query;
import com.openvehicletracking.collector.helper.HttpHelper;
import com.openvehicletracking.collector.http.domain.AccessToken;
import com.openvehicletracking.collector.http.domain.User;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;

/**
 * Created by oksuz on 09/07/2017.
 *
 */
public class AuthorizationFilter implements Handler<RoutingContext> {

    private HashSet<String> preAuthPaths = new HashSet<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);

    private AuthorizationFilter(HashSet<String> preAuthPaths) {
        this.preAuthPaths.addAll(preAuthPaths);
    }

    public static AuthorizationFilter create(HashSet<String> preAuthPaths) {
        return new AuthorizationFilter(preAuthPaths);
    }

    @Override
    public void handle(final RoutingContext context) {
        final HttpServerRequest request = context.request();
        final HttpServerResponse response = context.response();

        if (preAuthPaths.contains(request.path())) {
            LOGGER.debug("Path {} is pre authorised", request.path());
            context.next();
            return;
        }

        if (request.getHeader(AppConstants.HEADER_ACCESS_TOKEN) == null || Objects.equals("", request.getHeader(AppConstants.HEADER_ACCESS_TOKEN))) {
            HttpHelper.getUnauthorized(response).end();
            LOGGER.debug("access token header is empty");
            return;
        }

        String accessToken = request.getHeader(AppConstants.HEADER_ACCESS_TOKEN);
        Query query = new Query(MongoCollection.USERS)
                .addCondition("accessTokens.token", accessToken)
                .setFindOne(true);

        context.vertx().eventBus().<JsonObject>send(AppConstants.Events.NEW_QUERY, query, result -> {
            JsonObject userResult = result.result().body();
            if (result.failed()) {
                LOGGER.error("user query failed", result.cause());
                HttpHelper.getInternalServerError(response, result.cause().getMessage()).end();
                return;
            }

            if (userResult == null) {
                HttpHelper.getUnauthorized(response).end();
                return;
            }

            User user = User.fromMongoRecord(userResult);
            AccessToken userAccessToken = user.getAccessTokens().stream().filter(token -> Objects.equals(token.getToken(), accessToken)).findFirst().get();
            Date expireDate = new Date(userAccessToken.getExpireDate());
            if (expireDate.before(new Date())) {
                HttpHelper.getUnauthorized(response).end();
                return;
            }

            context.put("user", user);
            context.next();
        });
    }
}
