package com.openvehicletracking.collector.notification.sms;

import com.openvehicletracking.collector.notification.SendResult;
import com.openvehicletracking.core.GsonFactory;
import io.vertx.core.json.JsonObject;

public class SmsSendResult implements SendResult {

    private boolean failed;
    private boolean succeed;
    private String result;
    private Throwable cause;

    public static SmsSendResult Succeeded(String result) {
        SmsSendResult smsSendResult = new SmsSendResult();
        smsSendResult.succeed = true;
        smsSendResult.failed = false;
        smsSendResult.result = result;
        return smsSendResult;
    }

    public static SmsSendResult Failed(String result, Throwable cause) {
        SmsSendResult smsSendResult = new SmsSendResult();
        smsSendResult.succeed = false;
        smsSendResult.failed = true;
        smsSendResult.result = result;
        smsSendResult.cause = cause;
        return smsSendResult;
    }

    @Override
    public boolean failed() {
        return failed;
    }

    @Override
    public boolean succeed() {
        return succeed;
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public JsonObject getExtra() {
        return null;
    }

    @Override
    public String asJsonString() {
        return GsonFactory.getGson().toJson(this);
    }
}
