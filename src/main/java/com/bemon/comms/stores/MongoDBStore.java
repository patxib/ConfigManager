package com.bemon.comms.stores;

        import com.bemon.comms.transports.ITransport;
        import com.bemon.comms.transports.MongoDBTransport;
        import com.mongodb.Block;
        import com.mongodb.client.MongoCollection;
        import com.mongodb.client.MongoCursor;
        import com.mongodb.client.MongoDatabase;
        import com.mongodb.client.model.changestream.ChangeStreamDocument;
        import org.bson.Document;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;

        import java.util.*;
        import java.util.concurrent.ConcurrentHashMap;
        import java.util.concurrent.CopyOnWriteArrayList;


public class MongoDBStore implements IStore<Document> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBStore.class);

    MongoDBTransport mongoDBTransport;

    MongoCollection<Document> map;

    Block<ChangeStreamDocument<Document>> updateBlock;

    CopyOnWriteArrayList<IListener> listeners;

    @Override
    public void load(ITransport iTransport, String storeName){
        listeners = new CopyOnWriteArrayList<>();
        mongoDBTransport = (MongoDBTransport)iTransport;
        map = mongoDBTransport.getTransport().getCollection(storeName);

        updateBlock = new Block<ChangeStreamDocument<Document>>() {
            @Override
            public void apply(ChangeStreamDocument<Document> documentChangeStreamDocument) {
                listeners.forEach(p->p.onEvent("Update", null, documentChangeStreamDocument.getFullDocument()));
            }
        };
        map.watch().forEach(updateBlock);
    }

    public Document get(String predicate){
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    @Override
    public Collection<Document> getAll(){

        MongoCursor<Document> mongoCursor =
                map.find()
                        .iterator();
        List<Document> list = new ArrayList<>();
        while(mongoCursor.hasNext()) list.add(mongoCursor.next());
        return list;
    }

    @Override
    public void addListener(IListener listener){
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    @Override
    public void put(String key, Document entry){ throw new RuntimeException("NOT IMPLEMENTED"); }
}
