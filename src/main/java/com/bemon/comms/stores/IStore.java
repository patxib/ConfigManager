package com.bemon.comms.stores;

import com.bemon.comms.transports.ITransport;

public interface IStore<T> {

    void loadTransport(ITransport iTransport);

    T get(String predicate);

}
