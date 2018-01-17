package com.bemon.comms.vaults;

import com.bemon.common.model.IParser;
import com.bemon.comms.connections.IConnection;
import com.bemon.comms.connections.MongoDBConnection;
import com.bemon.comms.listeners.IListener;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlUpdate;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


public class MongoDBVault<T> implements IVault<T,Document> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBVault.class);

    private static final String PACKAGE = "_package";
    private static final String TYPE = "_type";

    MongoDBConnection mongoDBConnection;

    MongoCollection<Document> map;

    Block<ChangeStreamDocument<Document>> updateBlock;

    CopyOnWriteArrayList<IListener> listeners;

    IParser<T, Document> parser;

    String vaultName;

    private static String KEY = "_key";

    @Override
    public void load(IConnection iConnection, IParser parser, String vaultName) {
        LOGGER.debug("Loading MongoDB Collections");
        listeners = new CopyOnWriteArrayList<>();
        mongoDBConnection = (MongoDBConnection) iConnection;
        map = mongoDBConnection.getConnection().getCollection(vaultName);
        this.vaultName = vaultName;
        LOGGER.debug("Collections loaded. Initialising listeners");
        this.parser = parser;

        updateBlock = documentChangeStreamDocument -> {
            if(documentChangeStreamDocument.getFullDocument()!=null)
                listeners.forEach(p -> p.onEvent("Update",
                    vaultName,
                    (String) documentChangeStreamDocument.getFullDocument().get(KEY),
                    null,
                    parser == null ? documentChangeStreamDocument.getFullDocument() : parser.parseTo(documentChangeStreamDocument.getFullDocument())));
        };

        Runnable watch = () -> map.watch().forEach(updateBlock);
        new Thread(watch).start();
    }

    @Override
    public void load(IConnection iConnection, String storeName) {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public T get(String predicate) {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    @Override
    public Collection<T> getAll() {

        MongoCursor<Document> mongoCursor =
                map.find()
                        .iterator();
        List<T> list = new CopyOnWriteArrayList<>();
        while (mongoCursor.hasNext())
            list.add(parser == null ? (T) mongoCursor.next() : parser.parseTo(mongoCursor.next()));
        return list;
    }

    @Override
    public void addListener(IListener listener) {
        listeners.add(listener);
    }

    @Override
    public void put(String key, T entry) {
        throw new RuntimeException("NOTIMPLEMENTED");
    }

    @Override
    public void update(T key, T value) {
        LOGGER.debug("Udpating {}-{}", key,value);
        if( parser == null) this.map.updateOne((Document)key,new Document("$set",(Document)value));
            else this.map.replaceOne(parser.parseFrom(key), parser.parseFrom(value) );
    }

        public void update(String predicate) {
            update(predicate, null);
        }


        public void update(String predicate, Queue<Object> valueList) {

        SqlNode node;
        try {
            LOGGER.debug("Analizing Update estatement {}", predicate);
             node = SqlParser.create(predicate).parseStmt();
        }catch(SqlParseException e){
            LOGGER.error("Wrong Update statement: {}", predicate);
            throw new RuntimeException("Wrong Update statement");
        }
            if (node.getKind().equals(SqlKind.UPDATE)){
                SqlUpdate update = (SqlUpdate)node;
                if(!update.getTargetTable().toString().equals(vaultName.toUpperCase())) throw new RuntimeException("Wrong Vault. Found "+update.getTargetTable()+". Expected "+vaultName);
                List columns = update.getTargetColumnList()
                        .getList()
                        .stream()
                        .map(SqlNode::toString)
                        .collect(Collectors.toList());
                List values = update.getSourceExpressionList()
                        .getList()
                        .stream()
                        .map(SqlNode::toString)
                        .collect(Collectors.toList());


                Document updateOperatorsDocument = new Document();
                for(int i=0; i<columns.size();i++) updateOperatorsDocument.append((String)columns.get(i),(!values.get(i).equals("?")?values.get(i):valueList.poll()));
                Document updateDocument = new Document().append("$set", updateOperatorsDocument);


                Map condition =  parseCondition((SqlBasicCall)update.getCondition(), new HashMap());
                LOGGER.debug("Conditions: " + condition);
                Document conditionDocument = new Document();

                condition.keySet().stream().forEach(p->conditionDocument.append(p.toString() ,
                        (!condition.get(p).equals("?")?condition.get(p):valueList.poll())));

                this.map.updateOne(conditionDocument,updateDocument);

            }else{
                throw new RuntimeException("Expected UPDATE. Found " + node.getKind());
            }
    }
    private static Map<String,String> parseCondition(SqlBasicCall call, Map map){
        if(call.getOperator().kind.equals(SqlKind.EQUALS)){
            map.put(call.getOperands()[0].toString().toLowerCase(), call.getOperands()[1].toString().toLowerCase());
        }else{
            if(call.getOperator().kind.equals(SqlKind.AND)){
                call.getOperandList().stream().forEach(p->parseCondition((SqlBasicCall)p,map));
            } else {
                System.out.println(call.getOperator().kind);
                throw new RuntimeException("NOTIMPLEMENTED");
            }
        }
        return map;
    }

}
