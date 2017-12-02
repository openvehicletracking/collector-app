package com.openvehicletracking.collector.notification;

public interface NotificationMessage {

    Recipient getRecipient();

    String getTitle();

    String getBody();

}
