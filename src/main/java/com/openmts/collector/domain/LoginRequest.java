package com.openmts.collector.domain;

import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Created by yo on 08/07/2017.
 */
public class LoginRequest {

    private HttpServerRequest request;
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginRequest.class);

    public LoginRequest(HttpServerRequest request) {
        this.request = request;
    }

    public String getUsername() {
        return request.getParam("username");
    }

    public String getEncodedPassword() {
        try {
            return sha1(request.getParam("password"));
        } catch (Exception e) {
            LOGGER.error("sha1 exception", e);
        }

        return null;
    }

    public String getPassword() {
        return request.getParam("password");
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
