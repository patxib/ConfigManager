package com.bemon.comms.stores;

import com.bemon.comms.transports.HazelcastTransport;
import com.bemon.comms.transports.ITransport;
import com.hazelcast.core.ReplicatedMap;
import com.mongodb.Block;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class HazelcastStore implements IStore<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastStore.class);

    HazelcastTransport hazelcastTransport;

    ReplicatedMap map;


    @Override
    public void load(ITransport iTransport, String storeName){
        hazelcastTransport = (HazelcastTransport)iTransport;
        map = hazelcastTransport.getTransport().getReplicatedMap(storeName);
    }

    @Override
    public Object get(String predicate){
        return map.get(predicate);
    }

    @Override
    public Collection<Object> getAll(){
        throw new RuntimeException("NOT IMPLEMENTED");
    }


    @Override
    public void addListener(IListener listener){
        throw new RuntimeException("NOT IMPLEMENTED");
    }


    @Override
    public void put(String key, Object entry){
        map.put(key, entry);
    }
}
