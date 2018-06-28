# Instructions

This sample is using **Gradle** (instead of Maven) to build the artefacts.

1. Make sure Kafka is running. From withing the `streams` folder run:

    ```
    $ docker-compose up -d
    ```

2. Create the topics:

    ```
    $ docker-compose exec kafka kafka-topics \
        --create \
        --zookeeper zookeeper:32181 \
        --replication-factor 1 \
        --partitions 1 \
        --topic temperatures-topic  

    $ docker-compose exec kafka kafka-topics \
        --create \
        --zookeeper zookeeper:32181 \
        --replication-factor 1 \
        --partitions 1 \
        --topic high-temperatures-topic    
    ```

3. From within the **project** folder build the application:

    ```
    $ docker container run --rm \
        -v "$PWD":/home/gradle/project \
        -v "$HOME"/.gradle:/home/gradle/.gradle \
        -w /home/gradle/project \
        gradle:4.8.0-jdk8-alpine gradle build
    ```

4. From within the **project** folder run the application:

    ```
    $ docker container run --rm \
        --net streams_streams-net \
        -v $(PWD)/build/libs/json-sample-0.1.0.jar:/app.jar \
        openjdk:8-jre-alpine java -jar /app.jar
    ```

5. Create some temperature readings:

    ```
    docker-compose exec kafka sh -c 'cat << EOF | kafka-console-producer \
        --broker-list kafka:9092 \
        --property "parse.key=true" \
        --property "key.separator=:" \
        --key-serializer=org.apache.kafka.common.serialization.StringSerializer \
        --value-serializer=org.apache.kafka.connect.json.JsonSerializer \
        --topic temperatures-topic
    "S1":{"station":"S1", "temperature": 10.2, "timestamp": 1}
    "S1":{"station":"S1", "temperature": 11.2, "timestamp": 2}
    "S1":{"station":"S1", "temperature": 11.1, "timestamp": 3}
    "S1":{"station":"S1", "temperature": 12.5, "timestamp": 4}
    "S2":{"station":"S2", "temperature": 15.2, "timestamp": 1}
    "S2":{"station":"S2", "temperature": 21.7, "timestamp": 2}
    "S2":{"station":"S2", "temperature": 25.1, "timestamp": 3}
    "S2":{"station":"S2", "temperature": 27.8, "timestamp": 4}
    EOF'
    ```

6. Read from the output topic:

    ```
    $ docker-compose exec kafka kafka-console-consumer \
        --bootstrap-server kafka:9092 \
        --key-deserializer=org.apache.kafka.common.serialization.StringDeserializer \
        --from-beginning \
        --topic high-temperatures-topic
    ```

