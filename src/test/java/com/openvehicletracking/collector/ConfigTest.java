package com.openvehicletracking.collector;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ConfigTest {


    Config config;

    @Before
    public void before() {
        config = Config.getInstance();
        config.load(new JsonObject(testConfing));
    }


    @Test
    public void getInt() {
        Assert.assertEquals(config.getInt("cache.redis.port"),6379);
    }

    @Test
    public void getString() {
        Assert.assertEquals(config.getString("vertx.haGroup"),"openvts");
    }


    @Test
    public void getJsonArray() {
        JsonArray arr = (JsonArray) config.get("verticles");
        Assert.assertEquals(arr.getJsonObject(0).getString("id"),"com.openvehicletracking.collector.verticle.TcpVerticle");
    }

    @Test
    public void getJsonObject() {
        JsonObject o = (JsonObject) config.get("vertx");
        Assert.assertFalse(o.getBoolean("clustered"));
    }

    @Test
    public void getOrDefault() {
        String val = config.getString("dummyKey", "defaultval");
        Assert.assertEquals("defaultval", val);
    }

    public static final String testConfing = "{\"serverPort\":9001,\"serverHost\":\"0.0.0.0\",\"database\":{\"mongodb\":{\"host\":\"localhost\",\"port\":27017,\"db_name\":\"openvts\"}},\"vertx\":{\"clustered\":false,\"haEnabled\":false,\"haGroup\":\"openvts\",\"workerPoolSize\":5,\"clusterHost\":\"openvts\",\"eventLoopPoolSize\":5},\"verticles\":[{\"id\":\"com.openvehicletracking.collector.verticle.TcpVerticle\",\"options\":{\"instances\":1,\"worker\":false,\"ha\":true}},{\"id\":\"com.openvehicletracking.collector.verticle.MongoVerticle\",\"options\":{\"instances\":2,\"worker\":true,\"ha\":true}}],\"cache\":{\"redis\":{\"host\":\"localhost\",\"port\":6379}}}\n";
}