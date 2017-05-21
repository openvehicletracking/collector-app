package net.motodev.collector.verticle;

import io.vertx.core.AbstractVerticle;
import net.motodev.core.Device;
import net.motodev.core.MessageHandler;
import net.motodev.core.MotodevCollector;

import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;

/**
 * Created by yo on 21/05/2017.
 */
abstract public class MotodevAbstractVerticle extends AbstractVerticle {

    protected MessageHandler findHandler(String message) {
        List<Device> devices = MotodevCollector.getInstance().getDevices();
        for (Device d : devices) {
            Vector<MessageHandler> handlers = d.handlers();
            for (MessageHandler handler : handlers) {
                Matcher matcher = handler.pattern().matcher(message);
                if (matcher.matches()) {
                    return handler;
                }
            }
        }

        return null;
    }

}
