package com.openvehicletracking.collector.notification;


import io.vertx.core.Handler;

public interface Sender {

    void send(NotificationMessage message, Handler<SendResult> handler);

}
