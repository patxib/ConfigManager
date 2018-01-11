package com.bemon.common.model;


import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GenericParser implements IParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericParser.class);

    private static String MAP = "com.bemon.common.model.Map";
    private static String TYPE = "_type";
    private static String CONTENT = "_content";

    @Override
    public Map parseItem(Document item) {
        if (item.get(TYPE).equals(MAP)){
            return parseKeyValue(item);
        }else{
            throw new RuntimeException("TYPE NOT SUPPORTED");
        }
    }

    private Map parseKeyValue(Document item){
        Document content = (Document)item.get(CONTENT);
        Map<String, Object> map = new HashMap<>();
        content.entrySet().stream().forEach(p->map.put(p.getKey(), p.getValue()));
        return map;
    }
}
