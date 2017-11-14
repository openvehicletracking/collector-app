package com.openvehicletracking.collector.http.filter;

import com.openvehicletracking.collector.helper.HttpHelper;
import com.openvehicletracking.collector.http.domain.User;
import com.openvehicletracking.collector.http.domain.UserDevice;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * Created by oksuz on 08/10/2017.
 *
 */
public class DeviceFilter implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceFilter.class);
    private String deviceIdparamName;

    public DeviceFilter(String deviceIdparamName) {
        this.deviceIdparamName = deviceIdparamName;
    }

    public static DeviceFilter create(String deviceIdparamName) {
        return new DeviceFilter(deviceIdparamName);
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest request = context.request();

        String deviceId = request.getParam(deviceIdparamName);
        User user = context.get("user");
        Optional<UserDevice> userDevice = user.getDevices().stream().filter(d -> Objects.equals(d.getSerial(), deviceId)).findFirst();

        if (userDevice.isPresent()) {
            UserDevice device = userDevice.get();
            context.put("device", device);
            context.next();
            return;
        }

        HttpHelper.getNotFound(context.response(), "user device not found").end();
    }
}
