package com.bemon.comms.listeners;

public interface IListener<V> {
    void onOscillate(String action, V oldParticle, V newParticle );
}
