# Kafka ETL Pipeline

## Running the Pipeline

Navigate to the demo app folder:

    $ cd ~/confluent-labs/module-03/ksql-etl

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

to interactively work with the DB (use password: `weatherapp123`):

```bash
$ docker run --rm -it \
    --net ksql-etl_ksql-net \
    postgres:10.2-alpine \
        psql -d weather -U weatherapp -h weather-db
```

e.g. add a station:

```sql
weather=# INSERT INTO stations(name, lattitude, longitude, elevation)
            VALUES('Antartica 4', 270, 85, 2130);
```

## List all Connectors

In a different terminal window navigate to the project folder and execute the following command:

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
        --data '{ "name": "weatherapp-source", "config": { "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector", "tasks.max": 1, "connection.url": "jdbc:postgresql://weather-db:5432/weather?user=weatherapp&password=weatherapp123", "table.whitelist": "stations,readings", "mode": "incrementing", "incrementing.column.name": "id", "topic.prefix": "postgres-" }}' \
        http://kafka-connect:8082/connectors
```

Note that the above configures a **whitelist** containing the tables `stations` and `readings` to be imported into Kafka.

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
ksql> CREATE STREAM stations(\
        id bigint, \
        name string, \
        longitude integer, \
        lattitude integer, \
        elevation integer) \
        WITH (kafka_topic='postgres-stations', value_format='AVRO', key='id');
```

```sql
ksql> CREATE STREAM readings(\
        id bigint, \
        station_id bigint, \
        reading_date string, \
        temperature double, \
        wind_speed double, \
        wind_direction integer) \
        WITH (kafka_topic='postgres-readings', value_format='AVRO', key='id');
```

Can we access the data?

```sql
ksql> SELECT * FROM stations LIMIT 3;
1530177293626 | null | 1 | Antarctica 1 | 85 | 0 | 2240
1530177293626 | null | 2 | Antarctica 2 | 87 | 90 | 1785
1530177293626 | null | 3 | Antarctica 3 | 92 | 180 | 2550
```

```sql
sql> select * from readings limit 10;
1530177293127 | null | 1 | 1 | 1530117308548 | -1.5306066274642944 | 24.87377166748047 | -4
1530177293127 | null | 2 | 1 | 1530117368548 | -0.8072234392166138 | 25.82440757751465 | -3
1530177293127 | null | 3 | 1 | 1530117428548 | -1.0869826078414917 | 25.181835174560547 | -4
1530177293128 | null | 4 | 1 | 1530117488548 | -1.1630247831344604 | 25.817140579223633 | -2
1530177293128 | null | 5 | 1 | 1530117548548 | -1.190529704093933 | 25.372943878173828 | -2
1530177293128 | null | 6 | 1 | 1530117608548 | -1.5277445316314697 | 24.925884246826172 | -2
1530177293128 | null | 7 | 1 | 1530117668548 | -1.3878551721572876 | 25.057619094848633 | -3
1530177293128 | null | 8 | 1 | 1530117728548 | -0.9681357145309448 | 25.31396484375 | -2
1530177293128 | null | 9 | 1 | 1530117788548 | -1.1543512344360352 | 25.040143966674805 | -4
1530177293128 | null | 10 | 1 | 1530117848548 | -0.6620040535926819 | 25.789499282836914 | -3
LIMIT reached for the partition.
Query terminated
```

Create some aggregates:

```sql
select station_id, \
    max(temperature) as max, \
    min(temperature) min, \
    count(*) count \
from readings \
group by station_id;
```

resulting in something like this:

```bash
1 | -0.5405425429344177 | -1.5382883548736572 | 507
1 | -0.5387284159660339 | -1.5382883548736572 | 1000
2 | -3.429884672164917 | -4.429297924041748 | 1000
3 | 4.804220676422119 | 3.8066322803497314 | 1000
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
$ docker-compose exec kafka kafka-topics \
    --describe \
    --topic test-topic \
    --zookeeper zookeeper:32181
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
    --max-messages 5
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
    --max-messages 5
```