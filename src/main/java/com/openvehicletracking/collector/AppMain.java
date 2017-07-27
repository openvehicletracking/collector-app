package com.openvehicletracking.collector;

import com.openvehicletracking.collector.verticle.*;
import com.openvehicletracking.core.OpenVehicleTracker;
import com.openvehicletracking.device.xtakip.XTakip;
import io.vertx.core.DeploymentOptions;
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
public class AppMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppMain.class);

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

        DeploymentOptions workerDeploymentOptions = new DeploymentOptions().setWorker(true).setInstances(2);
        DeploymentOptions tcpDeployOpts = new DeploymentOptions().setInstances(5);

        OpenVehicleTracker tracker = OpenVehicleTracker.create(jsonConf);
        tracker.getDeviceRegistry().register(new XTakip());


        tracker.deployVerticle(TcpVerticle.class, tcpDeployOpts);
        tracker.deployVerticle(HttpVerticle.class, tcpDeployOpts);
        tracker.deployVerticle(NewMessageVerticle.class, workerDeploymentOptions);
        tracker.deployVerticle(PersistVerticle.class, workerDeploymentOptions);
        tracker.deployVerticle(DeviceCommandVerticle.class, workerDeploymentOptions);
        tracker.deployVerticle(DeviceAlertVerticle.class, workerDeploymentOptions);
    }

}
