package com.openvehicletracking.collector.notification.sms;

import com.openvehicletracking.collector.notification.NotificationMessage;
import com.openvehicletracking.collector.notification.Recipient;

public class SmsMessage implements NotificationMessage {

    private Recipient recipient;
    private String body;

    public SmsMessage(Recipient recipient, String body) {
        this.recipient = recipient;
        this.body = body;
    }

    @Override
    public Recipient getRecipient() {
        return recipient;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getBody() {
        return body;
    }
}
