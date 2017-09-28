package com.openvehicletracking.collector.verticle;

import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.db.Query;
import com.openvehicletracking.collector.db.Record;
import com.openvehicletracking.collector.db.Result;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        client = MongoClient.createNonShared(vertx, config);

        EventBus eventBus = vertx.eventBus();

        MessageConsumer<Query> queryMessageConsumer = eventBus.consumer(AppConstants.Events.NEW_QUERY);
        MessageConsumer<Record> recordConsumer = eventBus.consumer(AppConstants.Events.PERSIST);
        MessageConsumer<Record> commandUpdateConsumer = eventBus.consumer(AppConstants.Events.COMMAND_UPDATE);

        queryMessageConsumer.handler(this::queryHandler);
        recordConsumer.handler(this::persistHandler);
        commandUpdateConsumer.handler(this::commandUpdateHandler);
    }

    private void commandUpdateHandler(Message<Record> recordMessage) {
        Record record = recordMessage.body();
        client.updateCollection(record.getCollection().getName(), record.getUpdateQuery().getQuery(), record.getRecord(), genericResultHandler());
    }

    private void persistHandler(Message<Record> recordMessage) {
        Record record = recordMessage.body();
        client.save(record.getCollection().getName(), record.getRecord(), genericResultHandler());
    }

    private void queryHandler(Message<Query> queryMessage) {
        Query query = queryMessage.body();
        FindOptions findOptions = query.getFindOptions();

        if (queryMessage.body().isFindOne()) {
            client.findOne(query.getCollection().getName(), query.getQuery(), null, getResultHandler(queryMessage));
        } else {
            client.findWithOptions(query.getCollection().getName(), query.getQuery(), findOptions, getResultHandler(queryMessage));
        }
    }

    private <T> Handler<AsyncResult<T>> getResultHandler(Message<Query> queryMessage) {
        return result -> {
            Result queryResult = new Result<>(result.result(), result.succeeded(), result.cause());
            queryMessage.reply(queryResult);
        };
    }


    private static <T> Handler<AsyncResult<T>> genericResultHandler() {
        return result -> {
            if (result.failed()) {
                LOGGER.error("Error executing query, " + result.cause().getMessage(), result.cause());
            }
        };
    }
}
