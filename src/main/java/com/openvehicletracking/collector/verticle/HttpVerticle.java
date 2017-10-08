package com.openvehicletracking.collector.verticle;

import com.openvehicletracking.collector.http.filter.AuthorizationFilter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

/**
 * Created by yo on 07/10/2017.
 */
public class HttpVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpVerticle.class);

    @Override
    public void start() throws Exception {
        LOGGER.info("Starting verticle {} on port {}", HttpVerticle.class.getSimpleName(), config().getInteger("httpPort"));
        HttpServer httpServer = vertx.createHttpServer();

        final String virtualPath = config().getString("virtualPath", "/api");

        HashSet<String> preAuthorisedPaths = new HashSet<>();
        preAuthorisedPaths.add(virtualPath + "/access-token");

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

        router.route(HttpMethod.GET, virtualPath + "/hello").handler(context -> {
            context.response().end(Json.encode(context.get("user")));
        });


        httpServer.requestHandler(router::accept).listen(config().getInteger("httpPort"));
    }
}
