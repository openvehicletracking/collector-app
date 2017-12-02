package com.openvehicletracking.collector.notification;

import com.openvehicletracking.core.JsonSerializeable;
import io.vertx.core.json.JsonObject;

public interface SendResult extends JsonSerializeable {

    boolean failed();

    boolean succeed();

    String getResult();

    Throwable getCause();

    JsonObject getExtra();
}
