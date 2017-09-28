package com.openvehicletracking.collector;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.MessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yo on 23/09/2017.
 */
public class VerticleDeployer {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerticleDeployer.class);

    private final Vertx vertx;

    public VerticleDeployer(VertxOptions options) {
        vertx  = Vertx.vertx(options);
    }

    public <T> void registerEventBusCodec(Class<T> clazz, MessageCodec codec) {
        vertx.eventBus().registerDefaultCodec(clazz, codec);
    }

    public <T extends AbstractVerticle> void  deployVerticle(Class<T> verticle, DeploymentOptions deploymentOptions) {
        vertx.deployVerticle(verticle.getCanonicalName(), deploymentOptions, result -> {
            if (result.succeeded()) {
                LOGGER.info("Verticle deployed {}", verticle.getCanonicalName());
            } else {
                LOGGER.error("Verticle deploy failed " + result.cause().getMessage(), result.cause());
            }
        });
    }

}
