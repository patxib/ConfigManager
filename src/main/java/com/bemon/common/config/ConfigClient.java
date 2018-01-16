package com.bemon.common.config;

import com.bemon.common.Tools;
import com.bemon.comms.connections.HazelcastConnection;
import com.bemon.comms.vaults.HazelcastVault;
import sun.security.krb5.Config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigClient {
    private HazelcastConnection hazelcastConnection;
    private  Map<String, HazelcastVault> hazelcastVaults;

    public static void main(String[] args) {

        ConfigClient configClient= new ConfigClient();


        Runnable run = new Runnable() {
            @Override
            public void run() {
                while (true){

                }
            }
        };

    }

    public ConfigClient(){
        init(null);
    }

    public ConfigClient(HazelcastConnection hazelcastConnection){
        init(hazelcastConnection);
    }

    private void init(HazelcastConnection hazelcastConnection){
        if(hazelcastConnection== null){
            hazelcastConnection = new HazelcastConnection();
            hazelcastConnection.setProperties(System.getProperties());
            hazelcastConnection.start();
        }
        this.hazelcastConnection = hazelcastConnection;
        hazelcastVaults = new ConcurrentHashMap<>();    }

    public void loadVault (String vault){
        hazelcastVaults.put(vault, new HazelcastVault());
        hazelcastVaults.get(vault).load(hazelcastConnection, null, vault);
    }

    public Object getProperty(String vault, String key){
        return hazelcastVaults.get(key);
    }

}
