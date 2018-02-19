package com.openvehicletracking.collector;

import io.vertx.core.*;
import io.vertx.core.eventbus.MessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by oksuz on 23/09/2017.
 */
public class VerticleDeployer {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerticleDeployer.class);

    private Vertx vertx;

    public VerticleDeployer(VertxOptions options, Handler<VerticleDeployer> completionHandler) {
        if (options.isClustered()) {
            Vertx.clusteredVertx(options, result -> {
                this.vertx = result.result();
                completionHandler.handle(this);
            });
        } else {
            vertx = Vertx.vertx(options);
            completionHandler.handle(this);
        }

    }

    public <T> void registerEventBusCodec(Class<T> clazz, MessageCodec codec) {
        vertx.eventBus().registerDefaultCodec(clazz, codec);
        LOGGER.debug("Registering event bus codec {}", clazz.getCanonicalName());
    }

    public <T> void  deployVerticle(Class<T> verticle, DeploymentOptions deploymentOptions) {
        vertx.deployVerticle(verticle.getCanonicalName(), deploymentOptions, result -> {
            if (result.succeeded()) {
                LOGGER.info("Verticle deployed {}", verticle.getCanonicalName());
            } else {
                LOGGER.error("Verticle deploy failed for " + verticle.getCanonicalName() + " cause : " + result.cause().getMessage(), result.cause());
            }
        });
    }

}
