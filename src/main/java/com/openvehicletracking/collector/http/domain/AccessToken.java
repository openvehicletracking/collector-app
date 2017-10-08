package com.openvehicletracking.collector.http.domain;

/**
 * Created by oksuz on 07/10/2017.
 *
 */
public class AccessToken {

    private String token;
    private long expireDate;

    public AccessToken(String token, long expireDate) {
        this.token = token;
        this.expireDate = expireDate;
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
