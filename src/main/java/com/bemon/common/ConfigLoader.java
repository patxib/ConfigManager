package com.bemon.common;

import com.bemon.comms.stores.HazelcastStore;
import com.bemon.comms.stores.MongoDBStore;
import com.bemon.comms.transports.HazelcastTransport;
import com.bemon.comms.transports.MongoDBTransport;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class ConfigLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);

    private MongoDBStore mongoDBStore;
    private HazelcastStore hazelcastStore;

    private static final String CONFIG_STORE = "configstore";

    private static final String PACKAGE = "_package";

    public static void main(String[] args){
        try {
            loadConfig(args[0]);
            MongoDBTransport mongoDBTransport = new MongoDBTransport();
            mongoDBTransport.setProperties(System.getProperties());
            mongoDBTransport.start();

            HazelcastTransport hazelcastTransport = new HazelcastTransport();
            hazelcastTransport.setProperties(System.getProperties());
            hazelcastTransport.start();

            ConfigLoader configLoader = new ConfigLoader();

            configLoader.loadTransports(mongoDBTransport, hazelcastTransport);

            configLoader.loadConfiguration();


        }catch(Exception e){
            LOGGER.error("Failed to init: " + e.toString());
            e.printStackTrace();
        }
    }

    public void loadTransports(MongoDBTransport mongoDBTransport,
                               HazelcastTransport hazelcastTransport){
        this.mongoDBStore = new MongoDBStore();
        mongoDBStore.load(mongoDBTransport, System.getProperty(CONFIG_STORE));
        this.hazelcastStore = new HazelcastStore();
        hazelcastStore.load(hazelcastTransport, System.getProperty(CONFIG_STORE));
    }


    public void loadConfiguration(){
        Collection<Document> list = mongoDBStore.getAll();

        list.stream().forEach(p-> hazelcastStore.put(p.get(PACKAGE).toString(),p));

    }

    public static void loadConfig(String filename) throws IOException {
        FileInputStream propFile = new FileInputStream( filename);
        Properties p = new Properties(System.getProperties());
        p.load(propFile);
        System.setProperties(p);
    }
}
