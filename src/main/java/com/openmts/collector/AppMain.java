package com.openmts.collector;

import com.openmts.collector.verticle.*;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import com.openmts.core.Motodev;
import com.openmts.device.xtakip.XTakip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by yo on 19/05/2017.
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

        Motodev motodev = Motodev.create(jsonConf);
        motodev.getDeviceRegistry().register(new XTakip());


        motodev.deployVerticle(TcpVerticle.class, tcpDeployOpts);
        motodev.deployVerticle(HttpVerticle.class, tcpDeployOpts);
        motodev.deployVerticle(NewMessageVerticle.class, workerDeploymentOptions);
        motodev.deployVerticle(PersistVerticle.class, workerDeploymentOptions);
        motodev.deployVerticle(DeviceCommandVerticle.class, workerDeploymentOptions);
        motodev.deployVerticle(DeviceAlertVerticle.class, workerDeploymentOptions);
    }

}
