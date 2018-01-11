package com.bemon.comms.connections;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MongoDBConnection implements IConnection<MongoDatabase> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBConnection.class);


    Properties properties;

    MongoClient mongoClient;

    MongoDatabase connection;

    @Override
    public void setProperties (Properties properties){
        this.properties = properties;
    }

    @Override
    public void start(){
        String[] ips = properties.getProperty("mongodb.rs.ips").split(",");
        String[] ports = properties.getProperty("mongodb.rs.ports").split(",");
        if(ips.length==0||ips.length!=ports.length) throw new RuntimeException("Invalid configuration");

        mongoClient = new MongoClient(IntStream.range(0,ips.length)
                .mapToObj(i->new ServerAddress(ips[i], new Integer(ports[i])))
                .collect(Collectors.toList()));
        connection = mongoClient.getDatabase(properties.getProperty("mongodb.rs.database"));
    }

    @Override
    public void stop(){
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    @Override
    public MongoDatabase getConnection(){
        return connection;
    }



}
