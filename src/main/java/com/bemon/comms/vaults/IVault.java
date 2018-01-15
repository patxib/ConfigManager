package com.bemon.comms.vaults;

import com.bemon.common.model.IParser;
import com.bemon.comms.connections.IConnection;
import com.bemon.comms.listeners.IListener;

import java.util.Collection;

public interface IVault<T,K> {

    void load(IConnection iConnection, IParser parser, String storeName);

    void load(IConnection iConnection, String storeName);

    T get(String predicate);

    Collection<T> getAll();

    void addListener(IListener listener);

    void put(String key, T entry);

    void update(T key, T value);
}
