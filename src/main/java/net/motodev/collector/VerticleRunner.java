package net.motodev.collector;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.function.Consumer;

/**
 * Created by oksuz on 28/01/2017.
 */
public class VerticleRunner {

    private static Vertx vertx = null;

    private static Vertx getInstance(VertxOptions vertxOptions) {

        if (null != vertx) {
            return vertx;
        }

        vertx = Vertx.vertx(vertxOptions);

        return vertx;
    }


    public static void run(Class klass, VertxOptions vertxOptions, DeploymentOptions deploymentOptions) {

        if (vertxOptions == null) {
            // Default parameter
            vertxOptions = new VertxOptions();
        }

        Consumer<Vertx> runner = vertx -> {
            try {
                if (deploymentOptions != null) {
                    vertx.deployVerticle(klass.getName(), deploymentOptions);
                } else {
                    vertx.deployVerticle(klass.getName());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };

        if (vertxOptions.isClustered()) {
            Vertx.clusteredVertx(vertxOptions, res -> {
                if (res.succeeded()) {
                    Vertx vertx = res.result();
                    runner.accept(vertx);
                } else {
                    res.cause().printStackTrace();
                }
            });
        } else {
            Vertx vertx = getInstance(vertxOptions);
            runner.accept(vertx);
        }

    }
}
