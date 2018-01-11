package com.bemon.comms.vaults;

import com.bemon.comms.connections.IConnection;
import com.bemon.comms.listeners.IListener;

import java.util.Collection;
import java.util.Map;

public interface IVault<T> {

    void load(IConnection iConnection, String storeName);

    T get(String predicate);

    Collection<T> getAll();

    void addListener(IListener listener);

    void put(String key, T entry);

    void put(Map<String, T> map);

}
