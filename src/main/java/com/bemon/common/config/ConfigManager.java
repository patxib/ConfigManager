package com.bemon.common.config;


import com.bemon.common.Tools;
import com.bemon.common.model.ConfigParser;
import com.bemon.common.model.IParser;
import com.bemon.comms.connections.HazelcastConnection;
import com.bemon.comms.connections.MongoDBConnection;
import com.bemon.comms.vaults.HazelcastVault;
import com.bemon.comms.vaults.MongoDBVault;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

    private MongoDBConnection mongoDBConnection;
    private MongoDBVault<Document> mongoDBVault;
    private HazelcastConnection hazelcastConnection;
    private Map<String, HazelcastVault> hazelcastVaults;


    public static void main(String[] args){
        try {
            loadConfig(args[0]);
            LOGGER.debug("Loading ConfigLoader with params: {}", args[0]);
            MongoDBConnection mongoDBConnection = new MongoDBConnection();
            mongoDBConnection.setProperties(System.getProperties());
            mongoDBConnection.start();

            HazelcastConnection hazelcastConnection = new HazelcastConnection();
            hazelcastConnection.setProperties(System.getProperties());
            hazelcastConnection.start();

            ConfigManager configManager = new ConfigManager(mongoDBConnection, hazelcastConnection);
            configManager.start();

            LOGGER.debug("Config loaded");

        }catch(Exception e){
            LOGGER.error("Failed to init: " + e.toString());
            e.printStackTrace();
        }
    }

    public ConfigManager(MongoDBConnection mongoDBConnection,
                               HazelcastConnection hazelcastConnection){
        this.mongoDBConnection = mongoDBConnection;
        this.hazelcastConnection = hazelcastConnection;
    }

    public void start(){
        load();

        loadConfiguration();

        monitorChanges();
    }

    private void load(){
        mongoDBVault = new MongoDBVault();
        mongoDBVault.load(mongoDBConnection, null, System.getProperty(Tools.CONFIG_VAULT));
        hazelcastVaults = new ConcurrentHashMap<>();
    }

    private void loadConfiguration(){
        Collection<Document> list = mongoDBVault.getAll();
        LOGGER.debug("Collections loaded: {} ", list);

        IParser<Map<String, Object>, Document> parser = new ConfigParser();

        list.stream().forEach(p->{
            LOGGER.debug("Loading Item {}", p.get(Tools.KEY));
            HazelcastVault<Object,Object> hazelcastVault = new HazelcastVault();

            hazelcastVault.load(hazelcastConnection, null, (String)p.get(Tools.PACKAGE));

            parser.parseTo((Document)p.get(Tools.CONTENT)).entrySet()
                .forEach(q-> hazelcastVault.put(q.getKey(),q.getValue()));
            LOGGER.debug("Info Loaded: {}", p.get(Tools.CONTENT));
            hazelcastVaults.put((String)p.get(Tools.KEY), hazelcastVault);
            }
        );

    }

    private void monitorChanges(){
            mongoDBVault.addListener((event, vaultName, key, oldEntry, newEntry) -> {
                if(!event.equals("Update")) throw new RuntimeException("NOT SUPPORTED");

                Document newEntryDocument = (Document) newEntry;
                IParser<Map<String, Object>, Document> parser = new ConfigParser();

                parser.parseTo((Document)newEntryDocument.get(Tools.CONTENT)).entrySet()
                        .stream()
                       .forEach(p->{
                    LOGGER.debug("Comparing[{}] with [{}]: [{}]",
                            p.getValue(),
                            hazelcastVaults.get(key).get(p.getKey()),
                            p.getValue().equals(hazelcastVaults.get(key).get(p.getKey())));
                    if(p.getValue().equals(hazelcastVaults.get(key).get(p.getKey())))
                        LOGGER.debug("Key {} not Updated ", p.getKey());
                    else {
                        LOGGER.debug("Key {} Updated to {}", p.getKey(), p.getValue());
                        hazelcastVaults.get(key).put(p.getKey(),p.getValue());
                    }
                });
        });
            hazelcastVaults.entrySet().stream().forEach(p-> {
                p.getValue().addListener((event, vaultName, key, oldEntry, newEntry) -> {

           /*       NON CALCITE VERSION
                    mongoDBVault.update(new Document().append(Tools.PACKAGE, vaultName),
                            new Document().append(Tools.CONTENT + "." + key, newEntry));*/
                    Queue valueQueue = new ArrayDeque<>();
                    valueQueue.add(newEntry);
                    valueQueue.add(vaultName);
                    mongoDBVault.update("UPDATE " +System.getProperty(Tools.CONFIG_VAULT) + " SET \"" + Tools.CONTENT + "." + key + "\" = ? WHERE " + Tools.PACKAGE + " = ?", valueQueue);
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
