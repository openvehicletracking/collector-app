package com.openvehicletracking.collector.http.domain;

import com.openvehicletracking.collector.helper.HashHelper;
import io.vertx.ext.web.RoutingContext;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by oksuz on 08/07/2017.
 *
 */
public class LoginRequest {

    private RoutingContext context;

    public LoginRequest(RoutingContext context) {
        this.context = context;
    }

    public String getUsername() {
        return context.getBodyAsJson().getString("username");
    }

    public String getEncodedPassword() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return HashHelper.sha1(context.getBodyAsJson().getString("password"));
    }
}
