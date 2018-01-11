package com.bemon.common.model;

        import org.bson.Document;

        import java.util.Map;

public interface IParser {

    Map parseItem(Document item);
//    AbstractMessage parseFrom(byte[] data) throws InvalidProtocolBufferException;
}
