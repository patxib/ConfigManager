package com.bemon.common.config;

import com.bemon.common.model.GenericParser;
import com.bemon.common.model.IParser;
import com.bemon.comms.listeners.IListener;
import com.bemon.comms.vaults.HazelcastVault;
import com.bemon.comms.vaults.MongoDBVault;
import com.bemon.comms.connections.HazelcastConnection;
import com.bemon.comms.connections.MongoDBConnection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.Doc;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


public class ConfigLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);

    private MongoDBConnection mongoDBConnection;
    private MongoDBVault mongoDBVault;
    private HazelcastConnection hazelcastConnection;
    private IParser parser;
    private Map<String, HazelcastVault> hazelcastVaults;


    private static final String CONFIG_VAULT = "configvault";

    private static final String PACKAGE = "_package";

    public static void main(String[] args){
        try {
            loadConfig(args[0]);
            LOGGER.debug("Loading ConfigLoader with params: {}", args[0]);
            MongoDBConnection mongoDBTransport = new MongoDBConnection();
            mongoDBTransport.setProperties(System.getProperties());
            mongoDBTransport.start();

            HazelcastConnection hazelcastTransport = new HazelcastConnection();
            hazelcastTransport.setProperties(System.getProperties());
            hazelcastTransport.start();

            ConfigLoader configLoader = new ConfigLoader(mongoDBTransport, hazelcastTransport);

            configLoader.load();

            configLoader.loadConfiguration();

            configLoader.monitorChanges();

            LOGGER.debug("Config loaded");

        }catch(Exception e){
            LOGGER.error("Failed to init: " + e.toString());
            e.printStackTrace();
        }
    }

    public ConfigLoader(MongoDBConnection mongoDBConnection,
                               HazelcastConnection hazelcastConnection){
        this.mongoDBConnection = mongoDBConnection;
        this.hazelcastConnection = hazelcastConnection;
    }

    public void load(){
        mongoDBVault = new MongoDBVault();
        mongoDBVault.load(mongoDBConnection, System.getProperty(CONFIG_VAULT));
        hazelcastVaults = new ConcurrentHashMap<>();
    }


    public void loadConfiguration(){
        Collection<Document> list = mongoDBVault.getAll();
        LOGGER.debug("Collections loaded: {} ", list);

        parser = new GenericParser();

        list.stream().forEach(p->{
                LOGGER.debug("Loading Item {}", p.get(PACKAGE));
                HazelcastVault hazelcastVault = new HazelcastVault();
                hazelcastVault.load(hazelcastConnection, p.get(PACKAGE).toString());
                hazelcastVault.put(parser.parseItem(p));
                LOGGER.debug("Info Loaded: {}", parser.parseItem(p));
                hazelcastVaults.put( p.get(PACKAGE).toString(), hazelcastVault);
                }
        );

    }

    public void monitorChanges(){
            mongoDBVault.addListener((event, oldEntry, newEntry) -> {
                if(!event.equals("Update")) throw new RuntimeException("NOT SUPPORTED");
                Map map =  parser.parseItem((Document) newEntry);
                map.keySet().stream().forEach(p->{
                    LOGGER.debug("Compariong[{}] with [{}]: []{}",
                            map.get(p),
                            hazelcastVaults.get(((Document)newEntry).get(PACKAGE)).get(p.toString()),
                            map.get(p).equals(hazelcastVaults.get(((Document)newEntry).get(PACKAGE)).get(p.toString())));
                    if(map.get(p).equals(hazelcastVaults.get(((Document)newEntry).get(PACKAGE)).get(p.toString())))
                        LOGGER.debug("Key {} not Updated ", p);
                    else {
                        LOGGER.debug("Key {} Updated to {}", p, map.get(p));
                        hazelcastVaults.get(((Document) newEntry).get(PACKAGE)).put(p.toString(), map.get(p));
                    }

                });
        });
    }

    public static void loadConfig(String filename) throws IOException {
        FileInputStream propFile = new FileInputStream( filename);
        Properties p = new Properties(System.getProperties());
        p.load(propFile);
        System.setProperties(p);
    }
}
