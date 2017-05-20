package net.motodev.collector.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import net.motodev.collector.domain.MessageRequest;
import net.motodev.collector.helper.HttpHelper;
import net.motodev.collector.helper.MongoHelper;
import net.motodev.core.adapter.GeoJsonResponseAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.List;

/**
 * Created by oksuz on 05/02/2017.
 */
public class HttpVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpVerticle.class);

    @Override
    public void start() throws Exception {
        LOGGER.info("Starting verticle {} on port {}", HttpVerticle.class.getSimpleName(), config().getInteger("httpPort"));
        HttpServer httpServer = vertx.createHttpServer();

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.route().handler(CorsHandler.create("*")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedMethod(HttpMethod.DELETE)
                .allowedHeader("Content-Type")
                .allowedHeader("Authorization")
                .allowedHeader("X-Requested-With"));

        router.route(HttpMethod.GET, "/api/messages/:deviceId").handler(this::getLastMessages);
        router.route(HttpMethod.GET, "/api/messages/:deviceId/geojson").handler(this::getLastMessagesAsGeoJson);

        httpServer.requestHandler(router::accept).listen(config().getInteger("httpPort"));
    }

    private void getLastMessagesAsGeoJson(RoutingContext context) {
        lastMessages(context, result -> {
            if (result.failed()) {
                LOGGER.error("an error occurred while making query", result.cause());
                HttpHelper.getInternalServerError(context.response(), "internal server error").end();
            }

            GeoJsonResponseAdapter geoJsonAdapter = new GeoJsonResponseAdapter();
            HttpHelper.getOK(context.response(), geoJsonAdapter.result(result.result()).toString());
        });
    }

    private void getLastMessages(RoutingContext ctx) {
        lastMessages(ctx, result -> {
            if (result.failed()) {
                LOGGER.error("an error occurred while making query", result.cause());
                HttpHelper.getInternalServerError(ctx.response(), "internal server error").end();
            }

            HttpHelper.getOK(ctx.response(), result.result().toString());
        });
    }

    private void lastMessages(RoutingContext ctx, Handler<AsyncResult<List<JsonObject>>> handler) {
        MessageRequest request = new MessageRequest(ctx.request());

        MongoHelper.Query query;
        try {
            query = MongoHelper.getLastMessagesQuery(request.getSize(), request.getStatus(), request.getDeviceId(), request.getFromDate(), request.getToDate());
        } catch (ParseException e) {
            HttpHelper.getBadRequest(ctx.response(), "invalid date format. date format must be " + MessageRequest.DATE_FORMAT);
            return;
        }

        MongoClient mongoClient = MongoClient.createNonShared(vertx, config().getJsonObject("database").getJsonObject("mongodb"));
        mongoClient.findWithOptions(MongoHelper.MESSAGES, query.getQuery(), query.getFindOptions(), result -> {
            handler.handle(result);
            mongoClient.close();
        });
    }

}
