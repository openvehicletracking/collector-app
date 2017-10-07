package com.openvehicletracking.collector.domain;

import com.openvehicletracking.collector.helper.HashHelper;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by oksuz on 08/07/2017.
 */
public class LoginRequest {

    private RoutingContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginRequest.class);

    public LoginRequest(RoutingContext context) {
        this.context = context;
    }

    public String getUsername() {
        return context.getBodyAsJson().getString("username");
    }

    public String getEncodedPassword() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return HashHelper.sha1(context.getBodyAsJson().getString("password"));
    }

    public String getPassword() {
        return context.getBodyAsJson().getString("password");
    }

}
