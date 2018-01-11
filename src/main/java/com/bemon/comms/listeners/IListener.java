package com.bemon.comms.listeners;

public interface IListener<T> {
    void onEvent(String event, T oldEntry, T newEntry);
}
