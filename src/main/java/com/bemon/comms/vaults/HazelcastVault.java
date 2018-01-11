package com.bemon.comms.vaults;

import com.bemon.comms.connections.HazelcastConnection;
import com.bemon.comms.connections.IConnection;
import com.bemon.comms.listeners.IListener;
import com.hazelcast.core.ReplicatedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class HazelcastVault implements IVault<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastVault.class);

    HazelcastConnection hazelcastConnection;

    ReplicatedMap map;


    @Override
    public void load(IConnection iConnection, String storeName){
        hazelcastConnection = (HazelcastConnection) iConnection;
        map = hazelcastConnection.getConnection().getReplicatedMap(storeName);
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

    @Override
    public void put(Map<String, Object> map) {
        map.entrySet().forEach(p->this.map.put(p.getKey(),p.getValue()));
    }
}
