package com.bemon.comms.listeners;

public interface IListener<T> {
    void onEvent(String event, String vault, String key, T oldEntry, T newEntry);
}
