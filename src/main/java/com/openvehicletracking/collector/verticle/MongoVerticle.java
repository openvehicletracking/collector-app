package com.openvehicletracking.collector.verticle;

import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.database.Record;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by oksuz on 23/09/2017.
 *
 */
public class MongoVerticle extends AbstractVerticle {

    private static Logger LOGGER = LoggerFactory.getLogger(MongoVerticle.class);

    private MongoClient client;

    @Override
    public void start() throws Exception {
        JsonObject config = config().getJsonObject("database").getJsonObject("mongodb");
        client = MongoClient.createShared(vertx, config, AppConstants.MONGO_POOL_NAME);

        //TODO: Automate this kind of things.
        //client.createIndexWithOptions(MongoCollection.ACCESS_TOKENS.getName(), new JsonObject().put("expireAt", 1), new IndexOptions().expireAfter(0L, TimeUnit.SECONDS), result -> {});

        EventBus eventBus = vertx.eventBus();
        MessageConsumer<Record> recordConsumer = eventBus.consumer(AppConstants.Events.PERSIST);
        recordConsumer.handler(this::persistHandler);
    }


    private void persistHandler(final Message<Record> message) {
        Record record =  message.body();
        if (record != null) {
            client.save(record.getCollection().getName(), record.getRecord(), saveResultHandler(message));
        }
    }

    private Handler<AsyncResult<String>> saveResultHandler(final Message<Record> message) {
        return result -> message.reply(createReplyFromAsyncResult(result));
    }

    private JsonObject createReplyFromAsyncResult(AsyncResult<?> asyncResult) {
        JsonObject result = new JsonObject();
        result.put("success", asyncResult.succeeded());

        if (asyncResult.failed() && asyncResult.cause() != null) {
            result.put("error", asyncResult.cause().getMessage());
        }

        Object opResult = asyncResult.result();
        if (opResult instanceof String) {
            result.put("result", (String) opResult);
        }
        return result;
    }

}