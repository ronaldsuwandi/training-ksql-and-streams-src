# Kafka ETL Pipeline

## Running the Pipeline

Run all containers/services. This includes:

* Zookeeper
* Kafka Broker
* Schema Registry
* Kafka Connect
* Weather App
* Weather DB

```bash
$ docker-compose up -d
```

to interactively work with the DB:

```bash
$ docker run --rm -it --net ksql-etl_ksql-net postgres:10.2-alpine \
    psql -d weather -U weatherapp -h weather-db
```

e.g. add a station:

```sql
weather=# INSERT INTO stations(name, lattitude, longitude, elevation)
            VALUES('Antartica 4', 270, 85, 2130);
```

## List all Connectors

```bash
$ docker-compose exec kafka-connect \
    curl kafka-connect:8082/connector-plugins | jq
```

## Configure a File Source Connector

```bash
$ docker-compose exec kafka-connect \
    curl -s -X POST \
        -H "Content-Type: application/json" \
        --data '{"name": "weatherapp-file-source", "config": {"connector.class":"org.apache.kafka.connect.file.FileStreamSourceConnector", "tasks.max":"1", "topic":"weatherapp-data", "file": "/files/input.txt"}}' \
        http://kafka-connect:8082/connectors
```

Create some data in the file `input.txt`:

```bash
$ docker-compose exec kafka-connect \
    sh -c 'seq 1000 > /files/input.txt'
```

Test status of connector :

```bash
$ docker-compose exec kafka-connect \
    curl -s -X GET http://kafka-connect:8082/connectors/weatherapp-file-source/status
```

Test if data arrived in Kafka:

```bash
$ docker-compose exec kafka kafka-console-consumer \
        --bootstrap-server kafka:9092 \
        --topic weatherapp-data \
        --from-beginning \
        --max-messages 10
```

## JDBC Source Connector

Define JDBC Source Connector to our `weather-db` Postgres database: 

```bash
$ docker-compose exec kafka-connect \
    curl -s -X POST \
        -H "Content-Type: application/json" \
        --data '{ "name": "weatherapp-source", "config": { "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector", "tasks.max": 1, "connection.url": "jdbc:postgresql://weather-db:5432/weather?user=weatherapp&password=weatherapp123", "table.whitelist": "stations", "mode": "incrementing", "incrementing.column.name": "id", "topic.prefix": "postgres-" }}' \
        http://kafka-connect:8082/connectors
```

Test status of connector :

```bash
$ docker-compose exec kafka-connect \
    curl -s -X GET http://kafka-connect:8082/connectors/weatherapp-source/status
```
Test if data arrived in Kafka:

```bash
$ docker-compose exec kafka kafka-console-consumer \
        --bootstrap-server kafka:9092 \
        --topic postgres-stations \
        --from-beginning \
        --max-messages 3
```

## Running KSQL CLI

Run the CLI in the same network as the other containers:

```bash
$ docker container run -it --rm \
    --name cli \
    -e STREAMS_BOOTSTRAP_SERVERS=kafka:9092 \
    -e STREAMS_SCHEMA_REGISTRY_HOST=schema-registry \
    -e STREAMS_SCHEMA_REGISTRY_PORT=8081 \
    --network ksql-etl_ksql-net \
    confluentinc/ksql-cli:4.1.0 \
    ksql http://ksql-server:8088
```

Set offset to **earliest**:

```bash
ksql> SET 'auto.offset.reset' = 'earliest';
```

Create a stream:

```sql
ksql> CREATE STREAM stations(station_id bigint, name string, longitude double, lattitude double, elevation double) \
        WITH (kafka_topic='postgres-stations', value_format='AVRO', key='station_id');
```

Can we access the data?

```sql
ksql> SELECT * FROM stations LIMIT 3;
```

## Create a topic in Kafka

Create the topic:

```bash
$ docker-compose exec kafka kafka-topics --create \
      --topic test-topic \
      --partitions 1 \
      --replication-factor 1 \
      --if-not-exists \
      --zookeeper zookeeper:32181
```

Describe the topic:

```bash
$ docker-compose exec kafka kafka-topics --describe --topic test-topic --zookeeper zookeeper:32181
```

Create some data in the topic:

```bash
$ docker-compose exec kafka /bin/sh -c 'echo "Hello from Kafka test-topic" | kafka-console-producer --broker-list kafka:9092 --topic test-topic'
```

Read the data in the topic:

```bash
$ docker-compose exec kafka kafka-console-consumer \
    --bootstrap-server kafka:9092 \
    --topic test-topic \
    --from-beginning
```

In KSQL create a stream:

```sql
ksql> CREATE STREAM test(message string) \
        WITH (kafka_topic='test-topic', value_format='DELIMITED');
```

## Kafka Avro

### Schema with one field.

Create some data:

```bash
$ docker-compose exec kafka-connect kafka-avro-console-producer \
    --broker-list kafka:9092 \
    --topic t1 \
    --property schema.registry.url=http://schema-registry:8081 \
    --property value.schema='{"type":"record","name":"myrecord","fields":[{"name":"f1","type":"string"}]}'
```

the add this to the console:

```json
{"f1": "value1"}
{"f1": "value2"}
{"f1": "value3"}
{"f1": "value4"}
{"f1": "value5"}
```

end with `ctrl-d`.

Now show the values:

```bash
$ docker-compose exec kafka-connect kafka-avro-console-consumer \
    --bootstrap-server kafka:9092 \
    --topic t1 \
    --property schema.registry.url=http://schema-registry:8081 \
    --from-beginning \
    --max-messages 10
```

### Schema with two fields.

Create some data:

```bash
$ docker-compose exec kafka-connect kafka-avro-console-producer \
    --broker-list kafka:9092 \
    --topic t2 \
    --property schema.registry.url=http://schema-registry:8081 \
    --property value.schema='{"type":"record","name":"myrecord","fields":[{"name":"f1","type":"string"},{"name":"f2","type":"string"}]}'
```

the add this to the console:

```json
{"f1": "value1", "f2": "other1"}
{"f1": "value2", "f2": "other2"}
{"f1": "value3", "f2": "other3"}
{"f1": "value4", "f2": "other4"}
{"f1": "value5", "f2": "other5"}
```

end with `ctrl-d`.

Now show the values:

```bash
$ docker-compose exec kafka-connect kafka-avro-console-consumer \
    --bootstrap-server kafka:9092 \
    --topic t2 \
    --property schema.registry.url=http://schema-registry:8081 \
    --from-beginning \
    --max-messages 10
```