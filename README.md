# spring-integration-kafka

## Apache Kafka Quickstart 

https://kafka.apache.org/quickstart

```bash
docker run \
  --rm \
  --detach \
  --name kafka \
  --hostname kafka \
  --network bridge \
  --publish 9092:9092 apache/kafka:3.7.0
```

```bash
kafka-topics.sh \
  --create \
  --topic quickstart-events \
  --partitions 2 \
  --bootstrap-server localhost:9092
```

```bash
kafka-topics.sh \
  --describe \
  --topic quickstart-events \
  --bootstrap-server localhost:9092
```

```bash
kafka-console-producer.sh \
  --topic quickstart-events \
  --batch-size 1 \
  --bootstrap-server localhost:9092
```

```bash
kafka-console-consumer.sh \
  --topic quickstart-events \
  --from-beginning \
  --bootstrap-server localhost:9092
```

```bash
mvn package && docker build -t wasp-kafka-consumer .

docker run \
  --rm \
  --network host \
  --env SERVER_PORT=8081 \
  --env SPRING_KAFKA_CONSUMER_CLIENT_ID="events-consumer-1" \
  --env SPRING_KAFKA_CONSUMER_GROUP_ID="events" \
  --env SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092" \
  wasp-kafka-consumer:latest
  
docker run \
  --rm \
  --network host \
  --env SERVER_PORT=8082 \
  --env SPRING_KAFKA_CONSUMER_CLIENT_ID="events-consumer-2" \
  --env SPRING_KAFKA_CONSUMER_GROUP_ID="events" \
  --env SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092" \
  wasp-kafka-consumer:latest
  
docker run \
  --rm \
  --network host \
  --env SERVER_PORT=8083 \
    --env SPRING_KAFKA_CONSUMER_CLIENT_ID="orders-consumer-1" \
  --env SPRING_KAFKA_CONSUMER_GROUP_ID="orders" \
  --env SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092" \
  wasp-kafka-consumer:latest
```

```bash
kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --group events \
  --group orders \
  --offsets
```
