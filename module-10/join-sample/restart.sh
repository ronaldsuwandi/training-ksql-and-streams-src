docker-compose down
docker volume prune -f
docker-compose up -d
sleep 10

docker-compose exec kafka kafka-topics \
    --create \
    --zookeeper zookeeper:32181 \
    --replication-factor 1 \
    --partitions 1 \
    --topic left-topic

docker-compose exec kafka kafka-topics \
    --create \
    --zookeeper zookeeper:32181 \
    --replication-factor 1 \
    --partitions 1 \
    --topic right-topic

docker-compose exec kafka kafka-topics \
    --create \
    --zookeeper zookeeper:32181 \
    --replication-factor 1 \
    --partitions 1 \
    --topic joined-topic