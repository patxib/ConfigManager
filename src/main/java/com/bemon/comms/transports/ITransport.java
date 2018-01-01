package com.bemon.comms.transports;

import java.util.Properties;

public interface ITransport<V> {

    void setProperties(Properties properties);

    void start();

    void stop();

    V getTransport();

}
