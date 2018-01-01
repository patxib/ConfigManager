package com.bemon.comms.stores;

import com.bemon.comms.transports.ITransport;
import com.bemon.comms.transports.MongoDBTransport;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MongoDBStore implements IStore<List<Document>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBStore.class);

    MongoDBTransport mongoDBTransport;

    public void loadTransport(ITransport iTransport){
        mongoDBTransport = (MongoDBTransport)iTransport;
    }

    public List<Document> get(String predicate){
        MongoCursor<Document> mongoCursor =
                mongoDBTransport.getTransport()
                        .get(predicate.split(",")[0])
                        .getCollection(predicate.split(",")[1])
                        .find()
                        .iterator();
        List<Document> list = new ArrayList<>();
        while(mongoCursor.hasNext()) list.add(mongoCursor.next());
        return list;
    }
}
