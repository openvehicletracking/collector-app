package com.openvehicletracking.collector;

import com.openvehicletracking.collector.codec.QueryCodec;
import com.openvehicletracking.collector.codec.RecordCodec;
import com.openvehicletracking.collector.codec.ResultCodec;
import com.openvehicletracking.collector.db.Query;
import com.openvehicletracking.collector.db.Record;
import com.openvehicletracking.collector.db.Result;
import com.openvehicletracking.collector.verticle.HttpVerticle;
import com.openvehicletracking.collector.verticle.MongoVerticle;
import com.openvehicletracking.collector.verticle.MessageProcessorVerticle;
import com.openvehicletracking.collector.verticle.TcpVerticle;
import com.openvehicletracking.core.DeviceRegistry;
import com.openvehicletracking.device.xtakip.XTakip;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
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

        DeploymentOptions workerDeploymentOptions = new DeploymentOptions()
                .setWorker(true)
                .setInstances(10)
                .setConfig(jsonConf);

        DeploymentOptions tcpDeployOpts = new DeploymentOptions()
                .setInstances(5)
                .setConfig(jsonConf);


        DeviceRegistry.getInstance().register(new XTakip());

        VerticleDeployer verticleDeployer = new VerticleDeployer(new VertxOptions());

        verticleDeployer.registerEventBusCodec(Record.class, new RecordCodec());
        verticleDeployer.registerEventBusCodec(Query.class, new QueryCodec());
        verticleDeployer.registerEventBusCodec(Result.class, new ResultCodec());

        verticleDeployer.deployVerticle(TcpVerticle.class, tcpDeployOpts);
        verticleDeployer.deployVerticle(MongoVerticle.class, workerDeploymentOptions);
        verticleDeployer.deployVerticle(MessageProcessorVerticle.class, workerDeploymentOptions);
        verticleDeployer.deployVerticle(HttpVerticle.class, tcpDeployOpts);

    }

}
