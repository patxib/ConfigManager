package com.bemon.comms.connections;


import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
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

        if (System.getProperty("hazelcast.member_name")!= null) cfg.setInstanceName(System.getProperty("hazelcast.member_name"));

        NetworkConfig networkConfig = cfg.getNetworkConfig();


        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getTcpIpConfig().setEnabled(false);
        joinConfig.getAwsConfig().setEnabled(false);
        joinConfig.getMulticastConfig().setEnabled(true);
        joinConfig.getMulticastConfig().setMulticastTimeoutSeconds(2);
        joinConfig.getMulticastConfig().setMulticastTimeToLive(32);

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

    public String getName(){return connection.getName();}

}
