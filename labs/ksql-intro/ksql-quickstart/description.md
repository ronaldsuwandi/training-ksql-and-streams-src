# Run the KSQL Quickstart

In this hands-on lab we're going to play with KSQL. We're running all necessary components such as Kafka or Zookeeper and KSQL server and CLI in Docker containers.

## Running the Confluent Platform

Let's run the Confluent platform consisting of Zookeeper, Kafka, KSQL server and CLI on a Docker host such as our laptop running **Docker for Windows/Mac**.

1. In our `module-03` folder create a subfolder `ksql-quickstart` and navigate to it:

    ```bash
    $ cd ~/confluent-labs/module-03
    $ mkdir ksql-quickstart && cd ksql-quickstart
    ```

2. Download the `docker-compose.yml` file from GitHub:

    ```bash
    $ curl -L https://bit.ly/2KdQl8Z -o docker-compose.yml
    ```

3. Browse through the `docker-compose.yml` file and try to identify the individual components of the application:

    ```bash
    $ less docker-compose.yml
    ```

4. Run the application with:                

    ```bash
    $ docker-compose up -d
    ```

5. Wait until all the services of the app are up and running:    

    ```bash
    $ docker-compose ps
    ```

## Running the KSQL CLI

1. Start the KSQL CLI.

    ```
    $ docker container run -it --rm \
        --hostname cli \
        -e STREAMS_BOOTSTRAP_SERVERS=kafka:29092 \
        -e STREAMS_SCHEMA_REGISTRY_HOST=schema-registry \
        -e STREAMS_SCHEMA_REGISTRY_PORT=8081 \
        --network ksql-quickstart_demo-net \
        confluentinc/ksql-cli:5.0.0 /bin/bash
    ```

    We're running the KSQL CLI from withing a container that we have attached to the network `ksql-quickstart_demo-net` on which the Confluent platform runs.

2. Within the above container start the KSQL CLI:

    ```bash
    root@cli:/# ksql http://ksql-server:8088
    ```

## Testing KSQL

Here we're going to experiment with various features of KSQL. We use the two topics `pageviews` and `users` that have been created in Kafka for this. 

For more details please see the [quick start](https://docs.confluent.io/current/ksql/docs/tutorials/basics-docker.html#ksql-quickstart-docker).

1. Create a stream and a table from two topics in Kafka:

    ```SQL
    ksql> CREATE STREAM pageviews_original (\
          viewtime bigint, \
          userid varchar, \
          pageid varchar \
        ) WITH (kafka_topic='pageviews', value_format='DELIMITED');

    ksql> CREATE TABLE users_original (\
          registertime BIGINT, \
          gender VARCHAR, \
          regionid VARCHAR, \
          userid VARCHAR\
        ) WITH \
        (kafka_topic='users', value_format='JSON', key = 'userid');

2. Use `DESCRIBE pageviews_original;` and `DESCRIBE users_original` to display the details of the stream and the table created above.

3. Use `SHOW streams;` and `SHOW tables;` to get a list of all streams or tables defined in KSQL.

4. Get some data:

    ```sql
    ksql> SELECT pageid FROM pageviews_original LIMIT 3;
    ```

5. Create an enriched stream using JOIN:

    ```sql
    ksql> CREATE STREAM pageviews_enriched AS \
        SELECT users_original.userid AS userid, pageid, regionid, gender \
        FROM pageviews_original LEFT JOIN users_original \
        ON pageviews_original.userid = users_original.userid;
    ```

6. When querying the above stream we should get something like this:

    ```bash
    ksql> SELECT * FROM pageviews_enriched LIMIT 10;
    1524125600038 | User_1 | User_1 | Page_32 | Region_9 | FEMALE
    1524125600759 | User_3 | User_3 | Page_66 | Region_7 | MALE
    1524125600832 | User_3 | User_3 | Page_46 | Region_7 | MALE
    1524125601058 | User_8 | User_8 | Page_57 | Region_4 | FEMALE
    1524125601545 | User_9 | User_9 | Page_80 | Region_8 | OTHER
    1524125602334 | User_9 | User_9 | Page_64 | Region_2 | FEMALE
    1524125602928 | User_4 | User_4 | Page_71 | Region_7 | OTHER
    1524125603445 | User_5 | User_5 | Page_23 | Region_3 | FEMALE
    1524125603902 | User_8 | User_8 | Page_68 | Region_4 | FEMALE
    1524125604117 | User_7 | User_7 | Page_65 | Region_7 | OTHER
    LIMIT reached for the partition.
    Query terminated
    ```

7. From the output of `SHOW QUERIES;` identify a query ID you would like to terminate. For example, if you wish to terminate query ID `CSAS_PAGEVIEWS_ENRICHED`:

    ```sql
    ksql> TERMINATE CSAS_PAGEVIEWS_ENRICHED;
    ```

8. To quit the KSQL CLI enter `exit` and hit return.
9. To quit the `confluentinc/ksql-cli` container enter `exit` again and hit return.

## More Tests of the Platform

1. Retrieve the data from topic `pageviews` using the console consumer:

    ```bash
    $ docker container run -it --rm \
        --network ksql-quickstart_demo-net \
        confluentinc/cp-enterprise-kafka:5.0.0 kafka-console-consumer \
            --topic pageviews \
            --bootstrap-server kafka:29092 \
            --from-beginning \
            --max-messages 3 \
            --property print.key=true
    ```

2. We can equally consume data from topic `users`:

    ```bash
    $ docker container run -it --rm \
        --network ksql-quickstart_demo-net \
        confluentinc/cp-enterprise-kafka:5.0.0 kafka-console-consumer \
            --topic users \
            --bootstrap-server kafka:29092 \
            --from-beginning \
            --max-messages 3 \
            --property print.key=true

3. Use the following command to use the Kafka console producer to create a topic `t1`:

    ```bash
    $ docker container run -it --rm \
        --network ksql-quickstart_demo-net \
        confluentinc/cp-enterprise-kafka:5.0.0 kafka-console-producer \
            --topic t1 \
            --broker-list kafka:29092 \
            --property parse.key=true \
            --property key.separator=:
    ```

    When the producer is running enter data (note that there is a warning message after the first line since the topic `t1` does not yet exist. You can ignore it):

    ```
    key1:v1,v2,v3
    key2:w1,w2,w3
    key3:x1,x2,x3
    ```

    Press `CTRL-c` to stop the producer.

4. Let's create a second topic `t2`:

    ```bash
    $ docker container run -it --rm \
        --network ksql-quickstart_demo-net \
        confluentinc/cp-enterprise-kafka:5.0.0 kafka-console-producer \
            --topic t2 \
            --broker-list kafka:29092  \
            --property parse.key=true \
            --property key.separator=:
    ```

    and add this data:

    ```
    key1:{"id":"key1","col1":"v1","col2":"v2","col3":"v3"}
    key2:{"id":"key2","col1":"v4","col2":"v5","col3":"v6"}
    key3:{"id":"key3","col1":"v7","col2":"v8","col3":"v9"}
    key1:{"id":"key1","col1":"v10","col2":"v11","col3":"v12"}
    ```

5. Optional: Try to get data from topic `t1` and `t2`.


9. To list all topics in Kafka use this command:

    ```bash
    $ docker container run -it --rm \
        --network ksql-quickstart_demo-net \
        confluentinc/cp-enterprise-kafka:5.0.0 kafka-topics \
            --bootstrap-server kafka:9092 zookeeper:32181 \
            --list
    ```

## Cleanup

1. Tear down the application and free resources:

    ```bash
    $ docker-compose down
    $ docker volume prune -f
    ```
