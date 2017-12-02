package com.openvehicletracking.collector.notification;

import io.vertx.core.json.JsonObject;

public interface Recipient {

    String getFullName();

    String getEmailAddress();

    String getGsmNumber();

    JsonObject getPreferences();
}
