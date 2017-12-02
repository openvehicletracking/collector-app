package com.openvehicletracking.collector.verticle;

import com.openvehicletracking.collector.http.Controller;
import com.openvehicletracking.collector.http.controller.MessagesController;
import com.openvehicletracking.collector.http.controller.PublicLocationController;
import com.openvehicletracking.collector.http.controller.UserController;
import com.openvehicletracking.collector.http.filter.AuthorizationFilter;
import com.openvehicletracking.collector.http.filter.DeviceFilter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

/**
 * Created by oksuz on 07/10/2017.
 *
 */
public class HttpVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpVerticle.class);

    @Override
    public void start() throws Exception {
        final int httpPort = config().getInteger("httpPort", 9090);

        LOGGER.info("Starting verticle {} on port {}", HttpVerticle.class.getSimpleName(), httpPort);
        HttpServer httpServer = vertx.createHttpServer();

        final String virtualPath = config().getString("virtualPath", "/api");


        HashSet<String> preAuthorisedPaths = new HashSet<>();
        preAuthorisedPaths.add(virtualPath + "/access-token");
        preAuthorisedPaths.add(virtualPath + "/f/.*");

        Controller.setConfig(config());

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
                .allowedHeader("X-Requested-With")
                .allowedHeader("X-Access-Token"));


        router.route().handler(AuthorizationFilter.create(preAuthorisedPaths));

        router.route(HttpMethod.GET, virtualPath + "/user").handler(Controller.of(UserController.class)::user);
        router.route(HttpMethod.GET, virtualPath + "/user/checkpoint").handler(Controller.of(UserController.class)::checkpoint);
        router.route(HttpMethod.POST, virtualPath + "/access-token").handler(Controller.of(UserController.class)::login);


        router.route(virtualPath + "/device/:deviceId/*").handler(DeviceFilter.create("deviceId"));
        router.route(HttpMethod.GET, virtualPath + "/device/:deviceId/state").handler(Controller.of(MessagesController.class)::state);
        router.route(HttpMethod.GET, virtualPath + "/device/:deviceId/last-messages").handler(Controller.of(MessagesController.class)::lastMessages);
        router.route(HttpMethod.POST, virtualPath + "/device/:deviceId/public-hash").handler(Controller.of(PublicLocationController.class)::createHash);
        router.route(HttpMethod.POST, virtualPath + "/device/:deviceId/share").handler(Controller.of(PublicLocationController.class)::shareLocationWithSms);

        router.route(HttpMethod.GET, virtualPath + "/f/:hash").handler(Controller.of(PublicLocationController.class)::publicLocation);


        httpServer.requestHandler(router::accept).listen(httpPort);
    }
}
