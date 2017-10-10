package com.openvehicletracking.collector.http.domain;

import com.google.gson.Gson;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Set;

/**
 * Created by oksuz on 07/10/2017.
 *
 */
public class User {

    private String id;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String password;
    private double createdAt;
    private double updatedAt;
    private boolean isActive;
    private List<UserDevice> devices;
    private Set<AccessToken> accessTokens;

    public static User fromJson(JsonObject user) {
        user.put("id", user.getJsonObject("_id").getString("$oid"));
        user.remove("_id");
        return new Gson().fromJson(user.toString(), User.class);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(double createdAt) {
        this.createdAt = createdAt;
    }

    public double getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(double updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<UserDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<UserDevice> devices) {
        this.devices = devices;
    }

    public Set<AccessToken> getAccessTokens() {
        return accessTokens;
    }

    public void setAccessTokens(Set<AccessToken> accessTokens) {
        this.accessTokens = accessTokens;
    }
}