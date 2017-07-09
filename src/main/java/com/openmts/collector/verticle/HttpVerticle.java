package com.openmts.collector.verticle;

import com.openmts.collector.controller.CreateAccessTokenController;
import com.openmts.collector.controller.DeviceStateController;
import com.openmts.collector.controller.LastMessagesController;
import com.openmts.collector.controller.LastMessagesGeoJsonController;
import com.openmts.collector.filter.AuthorizationFilter;
import com.openmts.core.MotodevAbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by oksuz on 05/02/2017.
 *
 */
public class HttpVerticle extends MotodevAbstractVerticle {

    private static final String VIRTUAL_PATH = "/api";

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpVerticle.class);

    @Override
    public void start() throws Exception {
        LOGGER.info("Starting verticle {} on port {}", HttpVerticle.class.getSimpleName(), config().getInteger("httpPort"));
        HttpServer httpServer = vertx.createHttpServer();

        List<String> allowedPaths = new CopyOnWriteArrayList<>();
        allowedPaths.add(VIRTUAL_PATH + "/access-token");

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

        router.route().handler(AuthorizationFilter.create(allowedPaths));



        router.route(HttpMethod.GET, VIRTUAL_PATH + "/messages/:deviceId").handler(LastMessagesController::new);
        router.route(HttpMethod.GET, VIRTUAL_PATH + "/messages/:deviceId/geojson").handler(LastMessagesGeoJsonController::new);

        router.route(HttpMethod.GET, VIRTUAL_PATH + "/device/meta/:deviceId").handler(DeviceStateController::new);

        router.route(HttpMethod.POST, VIRTUAL_PATH + "/access-token").handler(CreateAccessTokenController::new);


        httpServer.requestHandler(router::accept).listen(config().getInteger("httpPort"));
    }

}
