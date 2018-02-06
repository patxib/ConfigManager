package com.bemon.comms.vaults;

import com.bemon.common.model.IParser;
import com.bemon.comms.connections.HazelcastConnection;
import com.bemon.comms.connections.IConnection;
import com.bemon.comms.listeners.IListener;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;
import com.hazelcast.core.ReplicatedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;



public class HazelcastVault<T,K> implements IVault<T,K> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastVault.class);

    private HazelcastConnection hazelcastConnection;

    private ReplicatedMap map;

    private CopyOnWriteArrayList<IListener> listeners;

    private String vaultName;
    private IParser<T,K> parser;

    @Override
    public void load(IConnection iConnection, String vaultName){
        throw new RuntimeException("NOTIMPLEMENTED");
    }

    @Override
        public void load(IConnection iConnection, IParser parser, String vaultName){

        this.parser = parser;
        this.vaultName = vaultName;
        listeners = new CopyOnWriteArrayList<>();

        hazelcastConnection = (HazelcastConnection) iConnection;
        map = hazelcastConnection.getConnection().getReplicatedMap(vaultName);

        map.addEntryListener(new EntryListener() {
            @Override
            public void entryAdded(EntryEvent event) {
                if(!event.getMember().localMember()) {
                    listeners.forEach(p ->
                            p.onEvent("Add",
                                    vaultName,
                                    (String) event.getKey(),
                                    null,
                                    parser == null ? event.getValue() : parser.parseFrom(event.getValue())));
                }
            }

            @Override
            public void entryEvicted(EntryEvent event) {
                throw new RuntimeException("NOT IMPLEMENTED");
            }

            @Override
            public void entryRemoved(EntryEvent event) {
                if(!event.getMember().localMember()) {
                    listeners.forEach(p ->
                            p.onEvent("Remove",
                                    vaultName,
                                    (String) event.getKey(), parser == null ? event.getOldValue() : parser.parseFrom(event.getOldValue()),
                                    null));
                }
            }

            @Override
            public void entryUpdated(EntryEvent event) {
                if(!event.getMember().localMember()) {
                    listeners.forEach(p ->
                            p.onEvent("Update",
                                    vaultName,
                                    (String) event.getKey(),
                                    parser == null ? event.getOldValue() : parser.parseFrom(event.getOldValue()),
                                    parser == null ? event.getValue() : parser.parseFrom(event.getValue())));
                }
            }

            @Override
            public void mapCleared(MapEvent event) {
                throw new RuntimeException("NOT IMPLEMENTED");
            }

            @Override
            public void mapEvicted(MapEvent event) {
                throw new RuntimeException("NOT IMPLEMENTED");
            }
        });
    }

    @Override
    public T get(String predicate){
        return parser == null ? (T)map.get(predicate) : parser.parseTo((K)map.get(predicate));
    }

    @Override
    public Collection<T> getAll(){
        throw new RuntimeException("NOT IMPLEMENTED");
    }


    @Override
    public void addListener(IListener listener){
        listeners.add(listener);
    }


        @Override
        public void put(String key, T entry) {
        map.put(key, entry);

    }

    @Override
    public void update(T key, T value) {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

}
