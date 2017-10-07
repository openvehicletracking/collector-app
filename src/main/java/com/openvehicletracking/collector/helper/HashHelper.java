package com.openvehicletracking.collector.helper;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Created by yo on 07/10/2017.
 */
public class HashHelper {

    public static String sha1(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
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
