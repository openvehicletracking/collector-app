package com.openvehicletracking.collector.notification.sms;

import com.openvehicletracking.collector.notification.Recipient;
import io.vertx.core.json.JsonObject;

public class SmsRecipient implements Recipient {

    private String gsm;

    public SmsRecipient(String gsm) {
        this.gsm = gsm;
    }

    @Override
    public String getFullName() {
        return null;
    }

    @Override
    public String getEmailAddress() {
        return null;
    }

    @Override
    public String getGsmNumber() {
        return gsm;
    }

    @Override
    public JsonObject getPreferences() {
        return null;
    }
}
