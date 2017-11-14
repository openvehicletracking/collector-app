package com.openvehicletracking.collector.http.domain;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by oksuz on 07/10/2017.
 *
 */
public class AccessToken {

    private String token;
    private long expireDate;
    private long createdAt;

    public AccessToken(String token, long expireDate, long createdAt) {
        this.token = token;
        this.expireDate = expireDate;
        this.createdAt = createdAt;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static AccessToken createFor24Hours() {
        long time = new Date().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 24);
        return new AccessToken(UUID.randomUUID().toString(), calendar.getTime().getTime(), time);
    }

    public String getToken() {
        return token;
    }

    public AccessToken setToken(String token) {
        this.token = token;
        return this;
    }

    public long getExpireDate() {
        return expireDate;
    }

    public AccessToken setExpireDate(long expireDate) {
        this.expireDate = expireDate;
        return this;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public AccessToken setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessToken that = (AccessToken) o;

        if (expireDate != that.expireDate) return false;
        return token.equals(that.token);

    }

    @Override
    public int hashCode() {
        int result = token.hashCode();
        result = 31 * result + (int) (expireDate ^ (expireDate >>> 32));
        return result;
    }
}
