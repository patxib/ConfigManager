package com.bemon.comms.stores;

import com.bemon.comms.transports.ITransport;

import java.util.Collection;

public interface IStore<T> {

    void load(ITransport iTransport, String storeName);

    T get(String predicate);

    Collection<T> getAll();

    void addListener(IListener listener);

    void put(String key, T entry);

}
