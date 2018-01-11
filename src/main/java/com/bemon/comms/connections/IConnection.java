package com.bemon.comms.connections;

import java.util.Properties;

public interface IConnection<V> {

    void setProperties(Properties properties);

    void start();

    void stop();

    V getConnection();

}
