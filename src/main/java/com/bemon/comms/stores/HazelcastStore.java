package com.bemon.comms.stores;

import com.bemon.comms.transports.HazelcastTransport;
import com.bemon.comms.transports.ITransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class HazelcastStore implements IStore<Map> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastStore.class);

    HazelcastTransport hazelcastTransport;

    public void loadTransport(ITransport iTransport){
        hazelcastTransport = (HazelcastTransport)iTransport;
    }

    public Map get(String predicate){
        return hazelcastTransport.getTransport().getMap(predicate);
    }
}
