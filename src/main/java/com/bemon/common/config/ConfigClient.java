package com.bemon.common.config;

import com.bemon.comms.connections.HazelcastConnection;
import com.bemon.comms.vaults.HazelcastVault;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigClient {
    private HazelcastConnection hazelcastConnection;
    private  Map<String, HazelcastVault> hazelcastVaults;

    public static void main(String[] args) {

        ConfigClient configClient= new ConfigClient();
        configClient.loadVault("com.bemon.common.config.data");


        Runnable read = () -> {
            while (true){
                System.out.println("Valor: " + configClient.get("com.bemon.common.config.data", "clave"));
                try {
                    Thread.sleep(2000);
                }catch(InterruptedException e){
                    break;
                }

            }
        };

        new Thread(read).start();

        Runnable write = () -> {
            String value = "flip";
            while (true){
                configClient.put("com.bemon.common.config.data", "clave", value);
                try {
                    Thread.sleep(4000);
                }catch(InterruptedException e){
                    break;
                }

                if (value.equals("flip")) {
                    value = "flop";
                }else{
                    value="flip";
                }
            }
        };

        new Thread(write).start();
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

    public Object get(String vault, String key){
        return hazelcastVaults.get(vault).get(key);
    }

    public void put (String vault, String key, Object value){
        hazelcastVaults.get(vault).put(key, value);
    }

}
