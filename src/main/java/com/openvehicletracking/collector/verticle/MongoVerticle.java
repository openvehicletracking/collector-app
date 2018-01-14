package com.openvehicletracking.collector.verticle;

import com.openvehicletracking.collector.AppConstants;
import com.openvehicletracking.collector.database.MongoCollection;
import com.openvehicletracking.collector.database.Query;
import com.openvehicletracking.collector.database.Record;
import com.openvehicletracking.collector.database.UpdateResult;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;
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
        client.createIndexWithOptions(MongoCollection.ACCESS_TOKENS.getName(), new JsonObject().put("expireAt", 1), new IndexOptions().expireAfter(0L, TimeUnit.SECONDS), result -> {});

        EventBus eventBus = vertx.eventBus();

        MessageConsumer<Query> queryMessageConsumer = eventBus.consumer(AppConstants.Events.NEW_QUERY);
        MessageConsumer<Record> recordConsumer = eventBus.consumer(AppConstants.Events.PERSIST);
        MessageConsumer<Record> updateConsumer = eventBus.consumer(AppConstants.Events.UPDATE);
        MessageConsumer<Query> deleteConsumer = eventBus.consumer(AppConstants.Events.DELETE);

        queryMessageConsumer.handler(this::queryHandler);
        recordConsumer.handler(this::persistHandler);
        updateConsumer.handler(this::updateHandler);
        deleteConsumer.handler(this::deleteHandler);
    }

    private void updateHandler(Message<Record> recordMessage) {
        Record record = recordMessage.body();

        JsonObject condition = record.getCondition().getQuery();
        replaceIdToMongoId(condition);

        if (record.getUpdateOptions().isUpsert()) {
            client.replaceDocumentsWithOptions(record.getCollection().getName(), condition, record.getRecord(), record.getUpdateOptions(), getUpdateResultHandler(recordMessage));
        } else {
            client.updateCollectionWithOptions(record.getCollection().getName(), condition, record.getRecord(), record.getUpdateOptions(), getUpdateResultHandler(recordMessage));
        }
    }

    private void deleteHandler(Message<Query> queryMessage) {
        Query query = queryMessage.body();
        client.removeDocument(query.getCollection().getName(), query.getQuery(), genericResultHandler(null));
    }

    private void replaceIdToMongoId(JsonObject query) {
        if (query != null && query.containsKey("_id")) {
            String docId = query.getString("_id");
            query.put("_id", new JsonObject().put("$oid", docId)).remove("id");
        }
    }

    private Handler<AsyncResult<MongoClientUpdateResult>> getUpdateResultHandler(Message<Record> recordMessage) {
        return result -> {
            MongoClientUpdateResult mongoClientUpdateResult = result.result();
            if (mongoClientUpdateResult != null) {
                UpdateResult updateResult = new UpdateResult(mongoClientUpdateResult.getDocMatched(), mongoClientUpdateResult.getDocModified(), mongoClientUpdateResult.getDocUpsertedId());
                recordMessage.reply(updateResult);
                return;
            }

            LOGGER.error("Update query error", result.cause());
        };
    }

    private void persistHandler(Message<Record> recordMessage) {
        Record record = recordMessage.body();
        client.save(record.getCollection().getName(), record.getRecord(), genericResultHandler(recordMessage));
    }

    private void queryHandler(Message<Query> queryMessage) {
        Query query = queryMessage.body();
        FindOptions findOptions = query.getFindOptions();

        if (queryMessage.body().isFindOne()) {
            LOGGER.debug("Sending findOne query to {} - {}", query.getCollection().getName(), query.getQuery());
            client.findOne(query.getCollection().getName(), query.getQuery(), null, result -> {
                if (result.failed()) {
                    LOGGER.error("query error", result.cause());
                }
                queryMessage.reply(result.result());
            });
        } else {
            LOGGER.debug("Sending findWithOptions query to {} - {} with options {}", query.getCollection().getName(), query.getQuery(), findOptions.toJson());
            client.findWithOptions(query.getCollection().getName(), query.getQuery(), findOptions, result -> {
                if (result.failed()) {
                    LOGGER.error("query error", result.cause());
                }
                queryMessage.reply(new JsonArray(result.result()));
            });
        }
    }

    private static <T> Handler<AsyncResult<T>> genericResultHandler(Message<Record> recordMessage) {
        return result -> {
            if (recordMessage != null) {
                recordMessage.reply(result.result());
            }
            if (result.failed()) {
                LOGGER.error("Error executing query, " + result.cause().getMessage(), result.cause());
            }
        };
    }
}