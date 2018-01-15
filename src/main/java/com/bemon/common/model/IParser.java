package com.bemon.common.model;

public interface IParser<T,K> {
    T parseTo(K item);
    K parseFrom(T item);
}
