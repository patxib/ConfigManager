package com.bemon.comms.transports;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MongoDBTransport implements ITransport<Map<String, MongoDatabase>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBTransport.class);


    Properties properties;

    MongoClient mongoClient;

    Map<String, MongoDatabase> transport;

    @Override
    public void setProperties (Properties properties){
        this.properties = properties;
    }

    @Override
    public void start(){
        String[] ips = properties.getProperty("mongodb.rs.ips").split(",");
        String[] ports = properties.getProperty("mongodb.rs.ports").split(",");
        if(ips.length==0||ips.length!=ports.length) throw new RuntimeException("Invalid configuration");
        List<ServerAddress> servers =new ArrayList<>();

        mongoClient = new MongoClient(IntStream.range(0,ips.length)
                .mapToObj(i->new ServerAddress(ips[i], new Integer(ports[i])))
                .collect(Collectors.toList()));
        LOGGER.debug("Loading DBs");
        transport = Arrays.asList(properties.getProperty("mongodb.rs.databases").split(",")).
                stream().
                collect(Collectors.toConcurrentMap(p->p,p->mongoClient.getDatabase(p)));
        LOGGER.info("DBs loaded - {}", transport.keySet());
    }

    @Override
    public void stop(){
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    @Override
    public Map<String, MongoDatabase> getTransport(){
        return transport;
    }



}
