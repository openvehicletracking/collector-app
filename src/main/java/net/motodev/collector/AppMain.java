package net.motodev.collector;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import net.motodev.collector.verticle.DeviceCommandVerticle;
import net.motodev.collector.verticle.NewMessageVerticle;
import net.motodev.collector.verticle.TcpVerticle;
import net.motodev.core.MotodevCollector;
import net.motodev.device.XTakip;
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

        String config;
        try {
            config = new Scanner(new File(configFile), "UTF-8").useDelimiter("\\A").next();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


        JsonObject jsonConf = new JsonObject(config);
        LOGGER.debug("Config: {}", jsonConf.encodePrettily());


        MotodevCollector.getInstance().deviceRegistry().addDevice(new XTakip());

        DeploymentOptions commonWorkerDeplOpts = new DeploymentOptions();
        commonWorkerDeplOpts.setWorker(true).setInstances(20).setConfig(jsonConf);

        DeploymentOptions tcpDeployOpts = new DeploymentOptions();
        tcpDeployOpts.setInstances(10).setHa(true).setConfig(jsonConf);

        VerticleRunner.run(TcpVerticle.class, null, tcpDeployOpts);
        VerticleRunner.run(NewMessageVerticle.class, null, commonWorkerDeplOpts);
        VerticleRunner.run(DeviceCommandVerticle.class, null, commonWorkerDeplOpts);
    }

}
