cls

set MONGO_LOGS=c:/data/logs

start mongod --replSet "rs0" > %MONGO_LOGS%/mongo.log

mongo --eval "rs.initiate();"