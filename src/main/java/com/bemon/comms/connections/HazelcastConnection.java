package com.bemon.comms.connections;


import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class HazelcastConnection implements IConnection<HazelcastInstance> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastConnection.class);


    Properties properties;

    HazelcastInstance connection;

    @Override
    public void setProperties (Properties properties){
        this.properties = properties;
    }

    @Override
    public void start(){
        Config cfg = new Config();
        connection = Hazelcast.newHazelcastInstance(cfg);
    }

    @Override
    public void stop(){
        if (connection != null){
            connection.shutdown();
        }
    }

    @Override
    public HazelcastInstance getConnection(){
        return connection;
    }

}
