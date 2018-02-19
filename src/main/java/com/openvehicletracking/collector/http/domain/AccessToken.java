package com.openvehicletracking.collector.http.domain;

import com.openvehicletracking.core.GsonFactory;
import com.openvehicletracking.core.JsonSerializeable;
import io.vertx.core.json.JsonObject;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by oksuz on 07/10/2017.
 *
 */
public class AccessToken implements JsonSerializeable {

    private String expireAt;
    private String email;
    private String token;

    public AccessToken(String expireAt, String email, String token) {
        this.expireAt = expireAt;
        this.email = email;
        this.token = token;
    }


    public static AccessToken createFor24Hours(String email) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 24);
        return new AccessToken(calendar.getTime().toInstant().toString(), email, UUID.randomUUID().toString());
    }

    public String getEmail() { return email; }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public AccessToken setToken(String token) {
        this.token = token;
        return this;
    }

    public String getExpireAt() {
        return expireAt;
    }

    public AccessToken setExpireAt(String expireAt) {
        this.expireAt = expireAt;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessToken that = (AccessToken) o;

        if (!email.equals(that.getEmail())) return false;
        if (!expireAt.equals(that.getExpireAt())) return false;
        return token.equals(that.token);

    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }

    @Override
    public String asJsonString() {
        return GsonFactory.getGson().toJson(this);
    }

    public JsonObject toMongoRecord() {
        return new JsonObject()
                .put("expireAt", new JsonObject().put("$date", expireAt))
                .put("email", email)
                .put("token", token);
    }
}
