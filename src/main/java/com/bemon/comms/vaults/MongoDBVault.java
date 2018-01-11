package com.bemon.comms.vaults;

        import com.bemon.comms.connections.IConnection;
        import com.bemon.comms.connections.MongoDBConnection;
        import com.bemon.comms.listeners.IListener;
        import com.mongodb.Block;
        import com.mongodb.client.MongoCollection;
        import com.mongodb.client.MongoCursor;
        import com.mongodb.client.model.changestream.ChangeStreamDocument;
        import org.bson.Document;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;

        import java.util.*;
        import java.util.concurrent.CopyOnWriteArrayList;


public class MongoDBVault implements IVault<Document> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBVault.class);

    MongoDBConnection mongoDBConnection;

    MongoCollection<Document> map;

    Block<ChangeStreamDocument<Document>> updateBlock;

    CopyOnWriteArrayList<IListener> listeners;

    @Override
    public void load(IConnection iConnection, String storeName){
        LOGGER.debug("Loading MongoDB Collections");
        listeners = new CopyOnWriteArrayList<>();
        mongoDBConnection = (MongoDBConnection) iConnection;
        map = mongoDBConnection.getConnection().getCollection(storeName);
        LOGGER.debug("Collections loaded. Initialising listeners");

        updateBlock = new Block<ChangeStreamDocument<Document>>() {
            @Override
            public void apply(ChangeStreamDocument<Document> documentChangeStreamDocument) {
                LOGGER.debug("Listeners count {}", listeners.size());
                listeners.forEach(p->p.onEvent("Update", null, documentChangeStreamDocument.getFullDocument()));
            }
        };
        Runnable watch = () -> map.watch().forEach(updateBlock);
        new Thread(watch).start();
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
        listeners.add(listener);
    }

    @Override
    public void put(String key, Document entry){ throw new RuntimeException("NOT IMPLEMENTED"); }

    @Override
    public void put(Map<String, Document> map) {
        throw new RuntimeException("NOT IMPLEMENTED");
    }
}
