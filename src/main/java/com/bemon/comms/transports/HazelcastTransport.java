package com.bemon.comms.transports;


import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class HazelcastTransport implements ITransport<HazelcastInstance> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastTransport.class);


    Properties properties;

    HazelcastInstance transport;

    @Override
    public void setProperties (Properties properties){
        this.properties = properties;
    }

    @Override
    public void start(){
        Config cfg = new Config();
        transport = Hazelcast.newHazelcastInstance(cfg);
    }

    @Override
    public void stop(){
        if (transport != null){
            transport.shutdown();
        }
    }

    @Override
    public HazelcastInstance getTransport(){
        return transport;
    }

}
