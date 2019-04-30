"Using the KSQL REST API"
import time
from subprocess import Popen, PIPE
import requests
try:
    from confluent_kafka import Producer, Consumer, KafkaError
    confluent_kafka_loaded = True
except ImportError:
    print("confluent_kafka not installed, using console tools.")
    confluent_kafka_loaded = False

quotes = [
    "Kafka enables the Confluent Streaming Platform",
    "Confluent offers a Streaming Platform powered by Kafka",
    "Kafka Streams are cool",
    "Streaming allows for real-time processing of information",
    "I love Kafka"
]
output_topic = "quotes"
input_topic = "QUOTES_LOWER"
bootstrap_servers = "kafka:9092"


def produce_quotes():
    "Write the quotes onto the output_topic"
    print("------ Writing quotes to topic '" + output_topic + "' ------")
    if confluent_kafka_loaded:
        producer = Producer({"bootstrap.servers": bootstrap_servers})
        for quote in quotes:
            print("*** writing: " + quote)
            producer.produce(output_topic, value=quote)
        producer.flush()
    else:
        p = Popen(['kafka-console-producer',
                   '--topic', output_topic,
                   '--broker-list', bootstrap_servers],
                  stdin=PIPE)
        p.communicate(input="\n".join(quotes).encode("utf-8"))
    print("------ done writing quotes ------")


def post_expression(ksql):
    "POST to the KSQL REST API"
    headers = {
        "accept": "application/vnd.ksql.v1+json",
        "content-type": "application/vnd.ksql.v1+json"
        }
    data = {
        "ksql": ksql,
        "streamsProperties": {"ksql.streams.auto.offset.reset": "earliest"}
    }
    try:
        r = requests.post("http://ksql-server:8088/ksql", json=data, headers=headers)
    except:
        print("ERROR: " + str(r.status_code) + ", " + r.text)
        raise
    print(str(r.status_code) + ", " + r.text)


def call_ksql():
    "Configure stream quotes_orig"
    print("--------- Posting to KSQL Server ---------")
    ksql = ("CREATE STREAM quotes_orig (line STRING) "
            "WITH(KAFKA_TOPIC='quotes', "
            "VALUE_FORMAT='DELIMITED');")
    post_expression(ksql)

    # wait until KSQL server has created the stream
    time.sleep(2)

    ksql = "CREATE STREAM quotes_lower AS SELECT LCASE(line) FROM quotes_orig;"
    post_expression(ksql)
    print("--------- done posting to KSQL Server -----------")


def consume_lowercase_quotes():
    "Consume from input_topic"
    print("------ Reading from topic '" + input_topic + "' ------")
    if confluent_kafka_loaded:
        consumer = Consumer({
            "bootstrap.servers": bootstrap_servers,
            "group.id": "sample-group",
            "default.topic.config": {
                "auto.offset.reset": "smallest"
            }
        })
        consumer.subscribe([input_topic])

        while True:
            msg = consumer.poll(1.0)

            if msg is None:
                continue
            if msg.error():
                if msg.error().code() == KafkaError._PARTITION_EOF:
                    continue
                else:
                    print(msg.error())
                    break

            print('Received message: {}'.format(msg.value().decode('utf-8')))

        consumer.close()
    else:
        Popen(['kafka-console-consumer',
               '--topic', input_topic,
               '--bootstrap-server', bootstrap_servers,
               '--from-beginning']).wait()

if __name__ == "__main__":
    print(">>> Starting Python Kafka Client...")
    produce_quotes()
    call_ksql()
    consume_lowercase_quotes()
    print("<<< Ending Python Kafka Client...")
