package com.bemon.comms.stores;

    public interface IListener<T> {
        void onEvent(String event, T oldEntry, T newEntry);
}
