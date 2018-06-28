docker-compose up -d
sleep 10    # wait until Kafka is ready

docker-compose exec kafka kafka-topics \
    --create \
    --zookeeper zookeeper:32181 \
    --replication-factor 1 \
    --partitions 1 \
    --topic lines-topic

docker-compose exec kafka kafka-topics \
    --create \
    --zookeeper zookeeper:32181 \
    --replication-factor 1 \
    --partitions 1 \
    --topic word-count-topic