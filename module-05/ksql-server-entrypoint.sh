# First we're waiting until Kafka and the Schema registry are ready
echo "Waiting for Kafka to be ready..."
cub kafka-ready -b kafka:29092 1 20 
echo "Waiting for Confluent Schema Registry to be ready..."
cub sr-ready schema-registry 8081 20 
# echo Waiting a few seconds for topic creation to finish... 
sleep 2 

# now we start the KSQL server
/usr/bin/ksql-server-start /etc/ksql/ksql-server.properties