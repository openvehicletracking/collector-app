package com.openvehicletracking.collector.verticle;

import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.notification.AbstractSender;
import com.openvehicletracking.collector.notification.sms.SmsMessage;
import com.openvehicletracking.collector.notification.sms.SmsRecipient;
import com.openvehicletracking.collector.notification.sms.SmsSendResult;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Constructor;

/**
 * Created by oksuz on 23/11/2017.
 *
 */
public class SmsVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsVerticle.class);

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(AppConstants.Events.NEW_SMS, this::handleRequest);
    }

    private void handleRequest(final Message<JsonObject> jsonObjectMessage) {
        JsonObject message = jsonObjectMessage.body();

        if (!isValidMessage(message)) {
            String err = "message is not valid message object " + message;
            LOGGER.error(err);
            jsonObjectMessage.reply(SmsSendResult.Failed("invalid sms request", new Exception(err)));
            return;
        }

        AbstractSender sender;
        try {
            sender = getSender();
        } catch (Exception e) {
            jsonObjectMessage.reply(SmsSendResult.Failed("cannot create SmsSender Instance", new Exception(e)));
            return;
        }

        SmsRecipient recipient = new SmsRecipient(message.getString("gsm"));
        SmsMessage smsMessage = new SmsMessage(recipient, message.getString("body"));
        sender.send(smsMessage, r -> {
            jsonObjectMessage.reply(r);
        });
    }

    private boolean isValidMessage(JsonObject object) {
        return object != null && object.containsKey("body") && object.containsKey("gsm");
    }


    private AbstractSender getSender() throws Exception {
        JsonObject config = config().getJsonObject("sms");
        WebClient client = WebClient.create(vertx);

        Class cls = Class.forName(config.getString("smsSender"));
        Constructor constructor = cls.getConstructor(JsonObject.class, WebClient.class);

        return (AbstractSender) constructor.newInstance(config.getJsonObject("clientConfig"), client);
    }

}
