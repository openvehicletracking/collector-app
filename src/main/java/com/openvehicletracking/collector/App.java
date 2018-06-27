package com.openvehicletracking.collector;

import com.openvehicletracking.collector.codec.RecordCodec;
import com.openvehicletracking.collector.database.Record;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by oksuz on 19/05/2017.
 *
 */
public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        String configFile = System.getProperty("conf");
        if (configFile == null) {
            throw new RuntimeException("please specify your config file like -Dconf=/path/to/config.json");
        }

        LOGGER.info("Starting app with config file: {}", configFile);

        String config;
        try {
            config = new Scanner(new File(configFile), "UTF-8").useDelimiter("\\A").next();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


        JsonObject jsonConf = new JsonObject(config);
        LOGGER.debug("Config: {}", jsonConf.encodePrettily());
        Context.init(args, jsonConf);

//        HazelcastClusterManager clusterManager = new HazelcastClusterManager();
        VertxOptions vertxOptions = new VertxOptions(jsonConf.getJsonObject("vertx"));

//        String clusterHost = System.getProperty("cluster-host");
//        if (clusterHost != null) {
//            LOGGER.info("cluster host is {}", clusterHost);
//            vertxOptions.setClusterHost(clusterHost).setClusterPort(15701);
//        }

        new VerticleDeployer(vertxOptions, verticleDeployer -> {
            verticleDeployer.registerEventBusCodec(Record.class, new RecordCodec());

            JsonArray verticles = jsonConf.getJsonArray("verticles");
            verticles.forEach(v -> {
                JsonObject verticleConfig = (JsonObject)v;
                try {
                    Class<?> verticleClass = Class.forName(verticleConfig.getString("id"));
                    DeploymentOptions deploymentOptions = new DeploymentOptions(verticleConfig.getJsonObject("options"));
                    deploymentOptions.setConfig(jsonConf);
                    verticleDeployer.deployVerticle(verticleClass, deploymentOptions);
                } catch (ClassNotFoundException e) {
                    LOGGER.error("class not found", e);
                }
            });

        });



    }

}
