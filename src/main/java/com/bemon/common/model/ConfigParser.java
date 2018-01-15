package com.bemon.common.model;

import org.bson.Document;

import java.util.Map;
import java.util.stream.Collectors;

public class ConfigParser implements IParser<Map<String,Object>, Document>{
    @Override
    public Map<String,Object> parseTo(Document item) {
        return item.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Document parseFrom(Map<String,Object> item) {
        throw new RuntimeException("NOT IMPLEMENTED");
    }
}
