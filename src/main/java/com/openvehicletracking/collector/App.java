package com.openvehicletracking.collector;

import com.openvehicletracking.collector.codec.*;
import com.openvehicletracking.collector.db.Query;
import com.openvehicletracking.collector.db.Record;
import com.openvehicletracking.collector.db.UpdateResult;
import com.openvehicletracking.collector.notification.sms.SmsSendResult;
import com.openvehicletracking.core.DeviceRegistry;
import com.openvehicletracking.core.alert.Alert;
import com.openvehicletracking.device.xtakip.XTakip;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
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


        DeviceRegistry.getInstance().register(new XTakip());
        ClusterManager clusterManager = new HazelcastClusterManager();

        VertxOptions vertxOptions = new VertxOptions()
                .setClustered(true)
                .setClusterManager(clusterManager)
                .setEventLoopPoolSize(1)
                .setHAEnabled(true)
                .setWorkerPoolSize(1)
                .setHAGroup("openvehicletracking");

        String clusterHost = System.getProperty("cluster-host");
        if (clusterHost != null) {
            LOGGER.info("cluster host is {}", clusterHost);
            vertxOptions.setClusterHost(clusterHost).setClusterPort(15701);
        }

        new VerticleDeployer(vertxOptions, verticleDeployer -> {
            verticleDeployer.registerEventBusCodec(Record.class, new RecordCodec());
            verticleDeployer.registerEventBusCodec(Query.class, new QueryCodec());
            verticleDeployer.registerEventBusCodec(Alert.class, new AlertCodec());
            verticleDeployer.registerEventBusCodec(UpdateResult.class, new UpdateResultCodec());
            verticleDeployer.registerEventBusCodec(SmsSendResult.class, new SmsSendResultCodec());

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
