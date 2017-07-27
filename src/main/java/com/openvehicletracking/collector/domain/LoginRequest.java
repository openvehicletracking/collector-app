package com.openvehicletracking.collector.domain;

import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

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

    public String getEncodedPassword() {
        try {
            return sha1(context.getBodyAsJson().getString("password"));
        } catch (Exception e) {
            LOGGER.error("sha1 exception", e);
        }

        return null;
    }

    public String getPassword() {
        return context.getBodyAsJson().getString("password");
    }

    private String sha1(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Objects.requireNonNull(str);

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        byte[] b = messageDigest.digest(str.getBytes("UTF-8"));

        String result = "";
        for (byte aB : b) {
            result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
        }

        return result;
    }
}
